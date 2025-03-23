package com.example.querico.Fragments

import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.R
import com.example.querico.ViewModel.EditProfileViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var user: UserEntity
    private lateinit var editProfileViewModel: EditProfileViewModel

    // UI Components
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var showPasswordButton: ImageButton
    private lateinit var showConfirmPasswordButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var changeImageButton: Button

    // Password visibility control
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    // Image handling
    private var imageUri: Uri? = null
    private lateinit var imageUrlRef: String

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Upload the image to Firebase Storage
            uploadImage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the user from arguments
        user = arguments?.getSerializable("user") as UserEntity

        // Initialize ViewModel
        editProfileViewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        // Set up navigation controller
        val navHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize UI components
        profileImageView = view.findViewById(R.id.edit_profile_image)
        nameEditText = view.findViewById(R.id.edit_profile_name_input_text)
        emailEditText = view.findViewById(R.id.edit_profile_email_input_text)
        passwordEditText = view.findViewById(R.id.edit_profile_password)
        confirmPasswordEditText = view.findViewById(R.id.edit_profile_confirm_password)
        showPasswordButton = view.findViewById(R.id.show_password_button)
        showConfirmPasswordButton = view.findViewById(R.id.show_confirm_password_button)
        saveButton = view.findViewById(R.id.save_profile_changes_button)
        cancelButton = view.findViewById(R.id.cancel_changes_button)
        changeImageButton = view.findViewById(R.id.change_profile_image_button)

        // Set initial image URL from user
        imageUrlRef = user.profileImg

        // Fill form with user data
        nameEditText.setText(user.name)
        emailEditText.setText(user.email)

        // Load profile image
        android.util.Log.d("ImageDebug", "Loading profile image from URL: ${user.profileImg}")
        Glide.with(requireContext())
            .load(user.profileImg)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(profileImageView)

        // Set click listeners for password visibility toggle
        showPasswordButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(passwordEditText, isPasswordVisible)
            updateButtonDrawable(showPasswordButton, isPasswordVisible)
        }

        showConfirmPasswordButton.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPasswordEditText, isConfirmPasswordVisible)
            updateButtonDrawable(showConfirmPasswordButton, isConfirmPasswordVisible)
        }

        // Set click listener for image change
        changeImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Set click listener for save button
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Validate input
            if (validateInput(name, email, password, confirmPassword)) {
                // Create updated user entity
                val updatedUser = UserEntity(
                    user.uid,
                    name,
                    imageUrlRef,
                    email
                )

                // Call the ViewModel to update the user profile
                editProfileViewModel.editProfile(updatedUser, password) { isSuccessful ->
                    if (isSuccessful) {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        // Navigate back to profile
                        navController.navigate(R.id.action_global_profileFragment) // Replace with your actual navigation action
                    } else {
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Set click listener for cancel button
        cancelButton.setOnClickListener {
            navController.navigate(R.id.action_global_profileFragment) // Replace with your actual navigation action
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        // Only validate password if user is trying to change it
        if (password.isNotEmpty()) {
            if (password.length < 6) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return false
            }

            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }

    private fun togglePasswordVisibility(passwordEditText: EditText, isVisible: Boolean) {
        passwordEditText.transformationMethod = if (isVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }

        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun updateButtonDrawable(button: ImageButton, isVisible: Boolean) {
        val drawableId = if (isVisible) R.drawable.ic_show_password else R.drawable.ic_hide_password
        button.setImageResource(drawableId)
    }

    private fun uploadImage() {
        // Show loading indicator or disable buttons if needed
        saveButton.isEnabled = false

        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("profile_images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrlRef = downloadUri.toString()

                    // Load the image into ImageView
                    Glide.with(requireContext()).load(imageUrlRef).into(profileImageView)

                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }.addOnFailureListener { e ->
                    // Handle failed download URL retrieval
                    Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
            }.addOnFailureListener { e ->
                // Handle failed upload
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            }
        }
    }
}