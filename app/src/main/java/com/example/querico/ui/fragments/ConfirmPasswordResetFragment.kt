package com.example.querico.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.querico.R
import com.example.querico.activities.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class ConfirmPasswordResetFragment : Fragment() {

    private lateinit var resetCodeEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var confirmButton: Button

    private var verificationCode: String? = null
    private var email: String? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationCode = it.getString(ARG_VERIFICATION_CODE)
            email = it.getString(ARG_EMAIL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_password_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // אתחול שדות
        resetCodeEditText = view.findViewById(R.id.confirm_password_reset_code_input_text)
        newPasswordEditText = view.findViewById(R.id.confirm_password_reset_password_input_text)
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_reset_password_confirm_input_text)
        confirmButton = view.findViewById(R.id.confirm_password_reset_button)

        // מילוי קוד האימות אם התקבל
        verificationCode?.let {
            resetCodeEditText.setText(it)
        }

        // הגדרת מאזינים
        setupListeners()
    }

    private fun setupListeners() {
        confirmButton.setOnClickListener {
            confirmPasswordReset()
        }
    }

    private fun confirmPasswordReset() {
        val code = resetCodeEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // בדיקת תקינות
        if (code.isEmpty()) {
            resetCodeEditText.error = "Please enter the verification code"
            return
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.error = "Please enter your new password"
            return
        }

        if (newPassword.length < 6) {
            newPasswordEditText.error = "Password must be at least 6 characters"
            return
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        // הצגת מצב טעינה
        showLoading(true)

        // אימות קוד איפוס סיסמה עם Firebase
        email?.let { email ->
            try {
                auth.confirmPasswordReset(code, newPassword)
                    .addOnCompleteListener { task ->
                        showLoading(false)

                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password reset successful", Toast.LENGTH_SHORT).show()
                            // מעבר למסך התחברות
                            navigateToLogin()
                        } else {
                            // טיפול בשגיאות
                            val errorMessage = when (task.exception) {
                                is FirebaseAuthInvalidCredentialsException -> "Invalid code"
                                else -> "Failed to reset password: ${task.exception?.message}"
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            showLoading(false)
            Toast.makeText(context, "Email not provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        confirmButton.isEnabled = !isLoading
        // ניתן להוסיף כאן ProgressBar או מחוון טעינה אחר
    }

    private fun navigateToLogin() {
        // Navigate to LoginActivity instead of LoginFragment
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear the activity stack
        startActivity(intent)
        requireActivity().finish() // Close current activity
    }

    companion object {
        private const val ARG_VERIFICATION_CODE = "verification_code"
        private const val ARG_EMAIL = "email"

        @JvmStatic
        fun newInstance(email: String, verificationCode: String? = null) =
            ConfirmPasswordResetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EMAIL, email)
                    verificationCode?.let { putString(ARG_VERIFICATION_CODE, it) }
                }
            }
    }
}