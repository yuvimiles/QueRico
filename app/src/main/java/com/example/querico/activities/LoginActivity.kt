package com.example.querico.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.querico.Constants
import com.example.querico.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Check if user is already logged in
        if (auth.currentUser != null) {
            val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }

        // Get references to views
        val emailEditText = findViewById<TextInputEditText>(R.id.login_email_input_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.login_password_input_text)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerText = findViewById<TextView>(R.id.login_dont_have_account)

        // Set up login button click listener
        loginButton.setOnClickListener {
            // Get the email and password
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            // Validate the information
            if (validateSignIn(email, password)) {
                signIn(email, password)
            } else {
                Toast.makeText(
                    this,
                    "Please fill in all required fields correctly",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up register text click listener
        registerText.setOnClickListener {
            val registerActivityIntent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(registerActivityIntent)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                // Add intent activity to the next activity
                val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    this, "Login failed. Please check your email and password", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateSignIn(email: String, password: String): Boolean {
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.isNotEmpty()
        val isPasswordLongEnough = password.length

        return isEmailValid && isPasswordValid && (isPasswordLongEnough >= Constants.PASS_MIN_LENGTH)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(email).matches()
    }
}