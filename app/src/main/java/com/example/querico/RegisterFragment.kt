package com.example.querico.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.querico.R
import com.example.querico.data.model.User
import com.example.querico.data.remote.FirebaseService
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.UUID

class RegisterFragment : Fragment() {

    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var addImageButton: ImageButton
    private lateinit var registerButton: Button

    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val firebaseService = FirebaseService()

    // ActivityResultLauncher לבחירת תמונה
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
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // אתחול השדות
        fullNameEditText = view.findViewById(R.id.register_fullname_input_text)
        emailEditText = view.findViewById(R.id.register_email_input_text)
        passwordEditText = view.findViewById(R.id.register_password_input_text)
        confirmPasswordEditText = view.findViewById(R.id.register_confirm_password_input_text)
        addImageButton = view.findViewById(R.id.register_add_image)
        registerButton = view.findViewById(R.id.register_button)

        // הגדרת מאזינים
        setupListeners()
    }

    private fun setupListeners() {
        // מאזין לכפתור הוספת תמונה
        addImageButton.setOnClickListener {
            openImagePicker()
        }

        // מאזין לכפתור הרישום
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImage.launch(intent)
    }

    private fun registerUser() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // וידוא תקינות הקלט
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Please enter your full name"
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email"
            return
        }

        if (!isValidEmail(email)) {
            emailEditText.error = "Please enter a valid email address"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Please enter a password"
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        // הפעלת ספינר/מצב טעינה
        showLoading(true)

        // רישום המשתמש ב-Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // קבלת משתמש רשום
                    val firebaseUser = auth.currentUser

                    // עדכון הפרופיל (שם ותמונה)
                    if (firebaseUser != null) {
                        val profileUpdates = if (selectedImageUri != null) {
                            // אם יש תמונה לעלות, נעלה אותה קודם
                            uploadProfileImageAndUpdateUser(firebaseUser.uid, fullName)
                        } else {
                            // אם אין תמונה, נעדכן רק את השם
                            updateUserProfile(firebaseUser.uid, fullName, null)
                            showLoading(false)
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            navigateToFeed()
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(context, "Failed to get user info", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // טיפול בשגיאות רישום
                    showLoading(false)
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "This email is already registered"
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Invalid email format"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun uploadProfileImageAndUpdateUser(userId: String, fullName: String) {
        selectedImageUri?.let { uri ->
            try {
                // העלאת התמונה ל-Firebase Storage
                firebaseService.uploadImage(uri, "profile_images/${UUID.randomUUID()}")
                    .addOnSuccessListener { downloadUrl ->
                        // עדכון פרופיל המשתמש עם ה-URL של התמונה
                        updateUserProfile(userId, fullName, downloadUrl.toString())
                        showLoading(false)
                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                        navigateToFeed()
                    }
                    .addOnFailureListener { e ->
                        // אם העלאת התמונה נכשלת, עדכן את הפרופיל ללא תמונה
                        updateUserProfile(userId, fullName, null)
                        showLoading(false)
                        Toast.makeText(context, "Registered, but failed to upload image", Toast.LENGTH_SHORT).show()
                        navigateToFeed()
                    }
            } catch (e: Exception) {
                // טיפול בשגיאות העלאה
                updateUserProfile(userId, fullName, null)
                showLoading(false)
                Toast.makeText(context, "Registered, but failed to upload image", Toast.LENGTH_SHORT).show()
                navigateToFeed()
            }
        }
    }

    private fun updateUserProfile(userId: String, fullName: String, photoUrl: String?) {
        // עדכון פרופיל המשתמש ב-Firebase Auth
        val firebaseUser = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .apply {
                if (photoUrl != null) {
                    setPhotoUri(Uri.parse(photoUrl))
                }
            }
            .build()

        firebaseUser?.updateProfile(profileUpdates)

        // שמירת נתוני המשתמש ב-Firestore
        val user = User(
            id = userId,
            name = fullName,
            email = firebaseUser?.email ?: "",
            photoUrl = photoUrl,
            isCurrentUser = true
        )

        firebaseService.saveUser(user)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun showLoading(isLoading: Boolean) {
        registerButton.isEnabled = !isLoading
        // אפשר להוסיף פה ProgressBar
    }

    private fun navigateToFeed() {
        // ניווט למסך ה-Feed
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, FeedFragment.newInstance())
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }
}