package com.example.querico.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso

class ImageLoader(private val context: Context) {

    private val TAG = "ImageLoader"

    // מטמון של launchers כדי למנוע שחרור והקצאה מחדש
    private val launchers = mutableMapOf<Fragment, ActivityResultLauncher<Intent>>()

    fun loadImage(uri: Uri, imageView: ImageView) {
        try {
            Picasso.get()
                .load(uri)
                .centerCrop()
                .fit()
                .into(imageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image: ${e.message}")
            // במקרה של שגיאה, נסה להציג את התמונה באופן פשוט
            imageView.setImageURI(uri)
        }
    }

    fun loadImage(url: String, imageView: ImageView) {
        try {
            Picasso.get()
                .load(url)
                .centerCrop()
                .fit()
                .into(imageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URL: ${e.message}")
        }
    }

    fun pickImage(fragment: Fragment, callback: (Uri?) -> Unit) {
        try {
            Log.d(TAG, "Starting image picker setup")

            // בדוק אם כבר יש launcher רשום לפרגמנט זה
            if (!launchers.containsKey(fragment)) {
                // צור רשם חדש רק אם אין אחד קיים
                val launcher = fragment.registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                    ActivityResultCallback { result ->
                        if (result.resultCode == android.app.Activity.RESULT_OK) {
                            val uri = result.data?.data
                            Log.d(TAG, "Image selected: $uri")
                            callback(uri)
                        } else {
                            Log.d(TAG, "Image selection canceled or failed")
                            callback(null)
                        }
                    }
                )
                launchers[fragment] = launcher

                // הוסף מאזין לסגירת הפרגמנט כדי להסיר את ה-launcher כשהפרגמנט נסגר
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { lifecycleOwner ->
                    if (lifecycleOwner == null) {
                        launchers.remove(fragment)
                    }
                }
            }

            // השתמש ב-launcher
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }

            Log.d(TAG, "Launching image picker")
            launchers[fragment]?.launch(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Error in pickImage: ${e.message}", e)
            callback(null)
        }
    }
}