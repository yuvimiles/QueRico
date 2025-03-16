package com.example.querico.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.querico.R
import com.example.querico.data.model.User
import com.example.querico.ui.viewmodels.RegisterUserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    // UI elements
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var backButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var uploadImageButton: ImageButton
    private var progressBar: ProgressBar? = null

    // Image upload variables
    private var imageUri: Uri? = null
    private var imageUrl: String = ""

    // ViewModel
    private lateinit var registerViewModel: RegisterUserViewModel

    // Tag for logging
    private val TAG = "RegisterActivity"

    // Image picker launcher
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

        // Get ViewModel
        registerViewModel = ViewModelProvider(this)[RegisterUserViewModel::class.java]

        // Initialize UI
        initializeViews()

        // Set up listeners
        setupListeners()
    }

    private fun initializeViews() {
        // Initialize form components
        fullNameEditText = findViewById(R.id.register_fullname_input_text)
        fullNameInputLayout = findViewById(R.id.register_fullname_input_hint)
        emailEditText = findViewById(R.id.register_email_input_text)
        emailInputLayout = findViewById(R.id.register_email_input_hint)
        passwordEditText = findViewById(R.id.register_password_input_text)
        passwordInputLayout = findViewById(R.id.register_password_input_hint)
        confirmPasswordEditText = findViewById(R.id.register_confirm_password_input_text)
        confirmPasswordInputLayout = findViewById(R.id.register_confirm_password_input_hint)
        registerButton = findViewById(R.id.register_button)
        backButton = findViewById(R.id.register_back_button)
        profileImageView = findViewById(R.id.register_profile_image)
        uploadImageButton = findViewById(R.id.register_upload_image_button)

        // ProgressBar - אם קיים ב-layout
        try {
            progressBar = findViewById(R.id.register_progress_bar)
        } catch (e: Exception) {
            Log.w(TAG, "ProgressBar not found in layout: ${e.message}")
        }
    }

    private fun setupListeners() {
        // Register button
        registerButton.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }

        // Back button
        backButton.setOnClickListener {
            navigateToLogin()
        }

        // Image selection
        uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    private fun uploadImage() {
        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("profile_images/${System.currentTimeMillis()}.jpg")

            // Show loading state
            showLoading(true)
            Toast.makeText(this, "מעלה תמונה...", Toast.LENGTH_SHORT).show()

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully
                Toast.makeText(this, "התמונה הועלתה בהצלחה", Toast.LENGTH_SHORT).show()

                // Get the URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrl = downloadUri.toString()

                    // Load the image into ImageView
                    try {
                        Glide.with(this).load(imageUrl).into(profileImageView)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load image with Glide: ${e.message}")
                    }

                    showLoading(false)
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "שגיאה בקבלת כתובת התמונה: ${e.message}", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "שגיאה בהעלאת התמונה: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Reset previous error messages
        fullNameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        // Validation
        if (fullName.isEmpty()) {
            fullNameInputLayout.error = "נא להזין שם מלא"
            return false
        }

        if (email.isEmpty()) {
            emailInputLayout.error = "נא להזין כתובת אימייל"
            return false
        }

        if (!isValidEmail(email)) {
            emailInputLayout.error = "נא להזין כתובת אימייל תקינה"
            return false
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "נא להזין סיסמה"
            return false
        }

        if (password.length < 6) {
            passwordInputLayout.error = "הסיסמה חייבת להכיל לפחות 6 תווים"
            return false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "נא לאשר את הסיסמה"
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "הסיסמאות אינן תואמות"
            return false
        }

        return true
    }

    private fun registerUser() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // הצגת מצב טעינה
        showLoading(true)

        // יצירת אובייקט User עם הנתונים שהוזנו
        val user = User(
            id = "", // יוגדר אוטומטית ע"י Firebase
            name = fullName,
            email = email,
            photoUrl = if (imageUrl.isNotEmpty()) imageUrl else null,
            lastLoginTime = System.currentTimeMillis()
        )

        // קריאה ל-ViewModel לרישום המשתמש
        registerViewModel.register(user, password) { isSuccessful ->
            showLoading(false)

            if (isSuccessful) {
                // הרשמה הצליחה
                Toast.makeText(this, "ההרשמה הושלמה בהצלחה!", Toast.LENGTH_SHORT).show()
                // מעבר למסך התחברות
                navigateToLogin()
            } else {
                // הרשמה נכשלה
                Toast.makeText(this, "ההרשמה נכשלה. אנא נסה שוב או בדוק את הפרטים שהזנת.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToLogin() {
        // ניתן להשתמש ב-Intent ספציפי למסך ההתחברות שלך
        finish() // סגירת מסך ההרשמה וחזרה למסך הקודם (שהוא כנראה מסך התחברות)
    }

    private fun showLoading(isLoading: Boolean) {
        // הפעלה או ביטול של כפתור ההרשמה
        registerButton.isEnabled = !isLoading

        // הצגה או הסתרה של מחוון הטעינה אם קיים
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE

        // אפשר גם לשנות את הטקסט על הכפתור בזמן טעינה
        if (isLoading) {
            registerButton.text = "מבצע רישום..."
        } else {
            registerButton.text = getString(R.string.register_header)
        }
    }
}