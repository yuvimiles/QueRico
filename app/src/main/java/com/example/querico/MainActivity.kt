package com.example.querico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.querico.activities.LoginActivity
import com.example.querico.ui.fragments.FeedFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if user is logged in
        if (auth.currentUser == null) {
            // No user is logged in, redirect to LoginActivity
            redirectToLogin()
            return
        }

        // User is logged in, show the main content
        if (savedInstanceState == null) {
            val feedFragment = FeedFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, feedFragment)
                .commit()
        }

        // Check Firebase connection
        checkFirebaseConnection()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity so the user can't go back with the back button
    }

    private fun checkFirebaseConnection() {
        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        // Check Auth connection
        val user = auth.currentUser
        if (user != null) {
            Log.d("FirebaseTest", "✅ User logged in: ${user.email}")
        } else {
            Log.d("FirebaseTest", "❌ No user logged in")
        }

        // Check Firestore connection
        firestore.collection("test").get()
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ Firestore connected successfully!")
            }
            .addOnFailureListener {
                Log.e("FirebaseTest", "❌ Error connecting to Firestore", it)
            }

        // Check Storage connection
        val storageRef = storage.reference
        storageRef.listAll()
            .addOnSuccessListener {
                Log.d("FirebaseTest", "✅ Storage connected successfully!")
            }
            .addOnFailureListener {
                Log.e("FirebaseTest", "❌ Error connecting to Storage", it)
            }
    }
}