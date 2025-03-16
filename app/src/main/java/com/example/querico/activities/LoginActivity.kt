package com.example.querico.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.querico.R
import com.example.querico.data.firebase.UserFB
import com.example.querico.ui.fragments.PasswordResetFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.example.querico.MainActivity

class LoginActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var dontHaveAccountText: TextView // Changed from Button to TextView
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var googleLoginButton: LinearLayout // Changed from Button to LinearLayout

    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    private val userFB = UserFB()

    // Google Sign In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    // Password visibility flag
    private var isPasswordVisible = false

    // Tag for logging
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In
        setupGoogleSignIn()

        // Initialize views
        initializeViews()

        // Setup listeners
        setupListeners()

        // Check if user is already logged in
        checkCurrentUser()
    }

    private fun setupGoogleSignIn() {
        try {
            val webClientId = "1077075055533-if4gi424abq0pcdnf4clkabth4fbkekc.apps.googleusercontent.com"

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign In configured with client ID: $webClientId")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Google Sign In: ${e.message}", e)
        }
    }

    private fun initializeViews() {
        // Initialize all UI components from the XML layout
        emailInputLayout = findViewById(R.id.login_email_input_hint)
        emailEditText = findViewById(R.id.login_email_input_text)
        passwordInputLayout = findViewById(R.id.login_password_hint_layout)
        passwordEditText = findViewById(R.id.login_password_input_text)
        loginButton = findViewById(R.id.login_button)
        forgotPasswordText = findViewById(R.id.login_forgot_password)
        dontHaveAccountText = findViewById(R.id.login_dont_have_account)
        rememberMeCheckbox = findViewById(R.id.login_remember_checkbox)
        googleLoginButton = findViewById(R.id.login_google_login_button)
    }

    private fun checkCurrentUser() {
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null && rememberUserEnabled()) {
            // User is already logged in and "remember me" was checked
            navigateToMainActivity()
        }
    }

    private fun rememberUserEnabled(): Boolean {
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        return sharedPref.getBoolean("remember_user", false)
    }

    private fun setupListeners() {
        // Login button listener
        loginButton.setOnClickListener {
            attemptLogin()
        }

        // Forgot password text listener
        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Register text listener
        dontHaveAccountText.setOnClickListener {
            navigateToRegister()
        }

        // Google login button listener
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Clear previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null

        // Validate input
        if (!validateLoginInput(email, password)) {
            return
        }

        // Show loading state
        showLoading(true)

        // Login using UserFB
        userFB.login(email, password) { isSuccessful ->
            showLoading(false)

            if (isSuccessful) {
                // Login successful
                saveRememberMePreference(rememberMeCheckbox.isChecked)
                onLoginSuccess()
            } else {
                // Login failed
                onLoginFailure("Login failed. Please check your email and password.")
            }
        }
    }

    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            return false
        }

        if (!isValidEmail(email)) {
            emailInputLayout.error = "Please enter a valid email address"
            return false
        }

        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveRememberMePreference(remember: Boolean) {
        val sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("remember_user", remember)
            apply()
        }
    }

    private fun onLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun onLoginFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showForgotPasswordDialog() {
        // Hide login screen content
        findViewById<View>(R.id.login_content).visibility = View.GONE

        // Show fragment container
        findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, PasswordResetFragment.newInstance())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToRegister() {
        // Navigate to register screen
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun signInWithGoogle() {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Clear previous Google Sign In state
            googleSignInClient.signOut().addOnCompleteListener {
                // After sign out is complete, start a new sign in
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching Google Sign In: ${e.message}", e)
            Toast.makeText(this, "Error opening Google Sign In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            try {
                // Show loading state
                showLoading(true)

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d(TAG, "Google Sign In succeeded")

                    if (account != null) {
                        // Check if we have an ID token
                        if (account.idToken != null) {
                            firebaseAuthWithGoogle(account.idToken!!)
                        } else {
                            Log.e(TAG, "No ID token found in Google account")
                            onLoginFailure("Sign in error: No ID token found from Google")
                            showLoading(false)
                        }
                    } else {
                        Log.e(TAG, "Google account is null")
                        onLoginFailure("Sign in error: Google account not found")
                        showLoading(false)
                    }

                } catch (e: ApiException) {
                    // Handle specific error codes
                    val statusCode = e.statusCode
                    val errorMessage = when (statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in canceled"
                        GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error, check your internet connection"
                        GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign in failed, please try again"
                        GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Invalid account"
                        GoogleSignInStatusCodes.TIMEOUT -> "Sign in timed out"
                        else -> "Sign in error (code: $statusCode)"
                    }

                    Log.e(TAG, "Google sign in failed with code $statusCode: $errorMessage")
                    onLoginFailure(errorMessage)
                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in Google Sign In", e)
                onLoginFailure("Unexpected error during Google Sign In")
                showLoading(false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithCredential:success")

                    // Save user details if they don't exist yet
                    val user = auth.currentUser
                    if (user != null) {
                        saveGoogleUserToFirestore(user)
                    }

                    saveRememberMePreference(rememberMeCheckbox.isChecked)
                    onLoginSuccess()
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val errorMsg = task.exception?.message ?: "Authentication failed"
                    onLoginFailure("Google authentication error: $errorMsg")
                }
            }
    }

    // New function to save Google user details to Firestore
    private fun saveGoogleUserToFirestore(firebaseUser: FirebaseUser) {
        userFB.getUserByUid(firebaseUser.uid) { existingUser ->
            // If user doesn't exist in Firestore yet, save them
            if (existingUser == null) {
                userFB.userCollection(
                    email = firebaseUser.email ?: "",
                    img = firebaseUser.photoUrl?.toString() ?: "",
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: ""
                ) { success ->
                    if (success) {
                        Log.d(TAG, "Google user data saved to Firestore")
                    } else {
                        Log.e(TAG, "Failed to save Google user data to Firestore")
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loginButton.isEnabled = false
            googleLoginButton.isEnabled = false
            // You can add a ProgressBar here if needed
        } else {
            loginButton.isEnabled = true
            googleLoginButton.isEnabled = true
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()

            // Restore login screen content visibility
            findViewById<View>(R.id.login_content).visibility = View.VISIBLE

            // Hide fragment container
            findViewById<View>(R.id.fragment_container).visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}