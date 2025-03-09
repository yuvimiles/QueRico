package com.example.querico.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.querico.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class PasswordResetFragment : Fragment() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var resetButton: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_password_reset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // אתחול הרכיבים
        emailEditText = view.findViewById(R.id.password_reset_email_input_text)
        resetButton = view.findViewById(R.id.register_button) // שם לא אידיאלי בקובץ XML, אבל נשתמש בו

        // הגדרת מאזינים
        setupListeners()
    }

    private fun setupListeners() {
        resetButton.setOnClickListener {
            sendPasswordResetEmail()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = emailEditText.text.toString().trim()

        // בדיקת תקינות האימייל
        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email"
            return
        }

        if (!isValidEmail(email)) {
            emailEditText.error = "Please enter a valid email address"
            return
        }

        // הצגת מצב טעינה
        showLoading(true)

        // שליחת אימייל לאיפוס סיסמה
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Password reset email sent. Check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()

                    // מעבר למסך אישור הסיסמה החדשה
                    navigateToConfirmPasswordReset(email)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun showLoading(isLoading: Boolean) {
        resetButton.isEnabled = !isLoading
        // אפשר להוסיף פה ProgressBar
    }

    private fun navigateToConfirmPasswordReset(email: String) {
        val confirmFragment = ConfirmPasswordResetFragment.newInstance(email)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, confirmFragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = PasswordResetFragment()
    }
}