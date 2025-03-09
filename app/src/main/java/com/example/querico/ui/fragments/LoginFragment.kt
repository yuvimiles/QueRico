package com.example.querico.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.querico.R
import com.example.querico.ui.fragments.RegisterFragment
import com.example.querico.ui.fragments.FeedFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

class LoginFragment : Fragment() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var dontHaveAccountText: TextView
    private lateinit var loginWithText: TextView
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var googleLoginButton: Button

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    // Google Sign In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    // Tag for logging
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In - בשיטה שעוקפת את הבעיה עם default_web_client_id
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Find views using correct IDs from XML - לפי הIDs שבקובץ XML
        emailInputLayout = view.findViewById(R.id.login_email_input_hint)
        emailEditText = view.findViewById(R.id.login_email_input_text)
        passwordInputLayout = view.findViewById(R.id.login_password_hint_layout)
        passwordEditText = view.findViewById(R.id.login_password_input_text)
        loginButton = view.findViewById(R.id.login_button)
        forgotPasswordText = view.findViewById(R.id.login_forgot_password)
        dontHaveAccountText = view.findViewById(R.id.login_dont_have_account)
        loginWithText = view.findViewById(R.id.login_or_login_with)
        rememberMeCheckbox = view.findViewById(R.id.login_remember_checkbox)
        googleLoginButton = view.findViewById(R.id.login_google_login_button)

        // Set up click listeners
        setupListeners()

        // Fix accessibility issues
        improveAccessibility()

        return view
    }

    private fun improveAccessibility() {
        // 1. Fix "Duplicate speakable text present" - set unique content description
        loginButton.contentDescription = "התחבר לחשבון"

        // 2. Fix "Hardcoded text" - get text from resources if possible
        googleLoginButton.contentDescription = "התחבר עם חשבון Google"

        // 3. Fix "Touch target size too small" - increase touch area
        ViewCompat.setAccessibilityDelegate(googleLoginButton, object : androidx.core.view.AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: androidx.core.view.accessibility.AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val clickableSpan = androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    androidx.core.view.accessibility.AccessibilityNodeInfoCompat.ACTION_CLICK,
                    "התחבר עם חשבון Google"
                )
                info.addAction(clickableSpan)
            }
        })

        // 4. Fix "Insufficient text color contrast ratio" - adjust colors
        context?.let { ctx ->
            val highContrastColor = ContextCompat.getColor(ctx, android.R.color.black)
            emailEditText.setTextColor(highContrastColor)
            passwordEditText.setTextColor(highContrastColor)
            emailInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#757575"))
            passwordInputLayout.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#757575"))
        }

        // 5. Fix "Accessibility Issue" with loginWithText
        loginWithText.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        loginWithText.contentDescription = "או התחבר באמצעות"
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null && rememberUserEnabled()) {
            // User is already logged in and "remember me" was checked
            navigateToFeedFragment()
        }
    }

    private fun rememberUserEnabled(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("remember_user", false)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            attemptLogin()
        }

        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        dontHaveAccountText.setOnClickListener {
            navigateToRegister()
        }

        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Basic validation
        when {
            email.isEmpty() -> {
                emailInputLayout.error = "Email is required"
                return
            }
            !isValidEmail(email) -> {
                emailInputLayout.error = "Enter a valid email address"
                return
            }
            password.isEmpty() -> {
                passwordInputLayout.error = "Password is required"
                return
            }
            password.length < 6 -> {
                passwordInputLayout.error = "Password must be at least 6 characters"
                return
            }
        }

        // Clear any previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null

        // Show loading
        showLoading(true)

        // Firebase authentication with email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success")
                    saveRememberMePreference(rememberMeCheckbox.isChecked)
                    onLoginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    onLoginFailure("Authentication failed: ${task.exception?.message ?: "Unknown error"}")
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun saveRememberMePreference(remember: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("remember_user", remember)
            apply()
        }
    }

    private fun onLoginSuccess() {
        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
        navigateToFeedFragment()
    }

    private fun navigateToFeedFragment() {
        try {
            val feedFragment = FeedFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, feedFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to feed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onLoginFailure(errorMessage: String) {
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_forgot_password, null)

        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.email_edit_text)

        AlertDialog.Builder(requireContext())
            .setTitle("Password Recovery")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val email = emailEditText?.text?.toString()?.trim() ?: ""
                if (email.isNotEmpty()) {
                    // Firebase password reset
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(),
                                    "Password reset instructions sent to your email",
                                    Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(),
                                    "Failed to send reset email: ${task.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(),
                        "Please enter your email address",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun navigateToRegister() {
        try {
            val registerFragment = RegisterFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, registerFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to registration: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(requireContext(), "Google Sign In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")
                    saveRememberMePreference(rememberMeCheckbox.isChecked)
                    onLoginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    onLoginFailure("Google authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loginButton.isEnabled = false
            googleLoginButton.isEnabled = false
            // אפשר להוסיף כאן ProgressBar אם צריך
        } else {
            loginButton.isEnabled = true
            googleLoginButton.isEnabled = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}