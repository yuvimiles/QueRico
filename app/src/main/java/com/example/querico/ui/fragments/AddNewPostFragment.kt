package com.example.querico.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.querico.R
import com.example.querico.viewmodel.RestaurantViewModel
import com.google.android.material.textfield.TextInputEditText

class AddNewPostFragment : Fragment() {

    // ViewBinding או findViewById - אתחול שדות
    private lateinit var restaurantNameEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var contentEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var addImageButton: ImageButton
    private lateinit var createPostButton: Button

    // ViewModel לניהול נתונים
    private lateinit var viewModel: RestaurantViewModel

    // משתנה לשמירת URI של תמונה שנבחרה
    private var selectedImageUri: Uri? = null

    // מגדיר ActivityResultLauncher לבחירת תמונה מהגלריה
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                addImageButton.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // מחזיר את ה-View המנופח
        return inflater.inflate(R.layout.fragment_add_new_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[RestaurantViewModel::class.java]

        // אתחול השדות
        restaurantNameEditText = view.findViewById(R.id.new_post_restaurant_name_input_text)
        locationEditText = view.findViewById(R.id.new_post_location_input_text)
        contentEditText = view.findViewById(R.id.new_post_content)
        ratingBar = view.findViewById(R.id.ratingBar)
        addImageButton = view.findViewById(R.id.register_add_image)
        createPostButton = view.findViewById(R.id.new_post_button)

        // הגדרת מאזינים
        setupListeners()
    }

    private fun setupListeners() {
        // מאזין לשינוי דירוג
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(context, "Selected Rating: $rating stars", Toast.LENGTH_SHORT).show()
        }

        // מאזין ללחיצה על כפתור הוספת תמונה
        addImageButton.setOnClickListener {
            openImagePicker()
        }

        // מאזין ללחיצה על כפתור יצירת פוסט
        createPostButton.setOnClickListener {
            createNewPost()
        }
    }

    // פתיחת בוחר תמונות
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImage.launch(intent)
    }

    // יצירת פוסט חדש
    private fun createNewPost() {
        val name = restaurantNameEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val description = contentEditText.text.toString().trim()
        val rating = ratingBar.rating

        // וידוא תקינות הקלט
        if (name.isEmpty()) {
            restaurantNameEditText.error = "Please enter restaurant name"
            return
        }

        if (location.isEmpty()) {
            locationEditText.error = "Please enter location"
            return
        }

        // הצגת ספינר או תצוגת טעינה
        showLoading(true)

        // שימוש ב-ViewModel ליצירת פוסט חדש
        viewModel.createRestaurant(name, location, description, rating, selectedImageUri)
            .observe(viewLifecycleOwner) { success ->
                showLoading(false)

                if (success) {
                    Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                    // חזרה למסך הקודם
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Failed to create post. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // הצגת/הסתרת אינדיקציית טעינה
    private fun showLoading(isLoading: Boolean) {
        // כאן תוכל להוסיף ProgressBar או אינדיקציית טעינה אחרת
        createPostButton.isEnabled = !isLoading
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddNewPostFragment()
    }
}