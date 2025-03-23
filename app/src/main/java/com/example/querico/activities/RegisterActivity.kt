package com.example.querico.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.querico.Constants
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.R
import com.example.querico.ViewModel.RegisterUserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    private lateinit var registerUserViewModel: RegisterUserViewModel
    private lateinit var imageUrlRef: String
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                // Upload the image to Firebase Storage
                uploadImage()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        // Get ViewModel instance
        registerUserViewModel = ViewModelProvider(this)[RegisterUserViewModel::class.java]

        // Initialize default image URL (you can replace this with your default image URL)
        imageUrlRef = "https://firebasestorage.googleapis.com/v0/b/querico-app.appspot.com/o/default_profile.jpg?alt=media"

        // Get UI references
        val fullNameEditText: TextInputEditText = findViewById(R.id.register_fullname_input_text)
        val emailEditText: TextInputEditText = findViewById(R.id.register_email_input_text)
        val passwordEditText: TextInputEditText = findViewById(R.id.register_password_input_text)
        val confirmPasswordEditText: TextInputEditText = findViewById(R.id.register_confirm_password_input_text)
        profileImageView = findViewById(R.id.register_profile_image)
        val registerButton: Button = findViewById(R.id.register_button)
        val backButton: Button = findViewById(R.id.register_back_button)
        progressBar = findViewById(R.id.register_progress_bar)

        // Set upload image button click listener
        profileImageView.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Set register button click listener
        registerButton.setOnClickListener {
            // Get user input
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Validate user input
            if (validate(email, password, confirmPassword, fullName)) {
                // Show progress bar
                progressBar.visibility = android.view.View.VISIBLE

                // Create user entity and register
                val user = UserEntity("", fullName, imageUrlRef, email)
                registerUserViewModel.register(user, password) { isSuccessful ->
                    // Hide progress bar
                    progressBar.visibility = android.view.View.GONE

                    if (isSuccessful) {
                        // Show success message
                        Toast.makeText(
                            this,
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to login activity
                        val loginIntent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(loginIntent)
                        finish()
                    } else {
                        // Show failure message
                        Toast.makeText(
                            this,
                            "Registration failed. Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Please fill in all fields correctly",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set back button click listener
        backButton.setOnClickListener {
            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    private fun uploadImage() {
        progressBar.visibility = android.view.View.VISIBLE

        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("profile_images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrlRef = downloadUri.toString()

                    // Load the image into ImageView using Glide
                    Glide.with(this).load(imageUrlRef).into(profileImageView)

                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = android.view.View.GONE
                }.addOnFailureListener { e ->
                    // Handle failed download URL retrieval
                    Toast.makeText(this, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = android.view.View.GONE
                }
            }.addOnFailureListener { e ->
                // Handle failed upload
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun validate(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String
    ): Boolean {
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.isNotEmpty()
        val isPasswordLongEnough = password.length >= Constants.PASS_MIN_LENGTH
        val doPasswordsMatch = password == confirmPassword
        val isFullNameValid = fullName.isNotEmpty()

        return isEmailValid && isPasswordValid && isPasswordLongEnough && doPasswordsMatch && isFullNameValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }
}