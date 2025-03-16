package com.example.querico.data.firebase

import android.util.Log
import com.example.querico.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserFB {
    private val TAG = "UserFB"
    private lateinit var auth: FirebaseAuth

    /**
     * רישום משתמש חדש
     */

    fun register(email: String, password: String, callback: (Boolean) -> Unit) {
        auth = Firebase.auth
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // רישום המשתמש הצליח
                    Log.d(TAG, "User registration successful")
                    callback(true)
                } else {
                    // רישום המשתמש נכשל
                    val exception = task.exception
                    Log.e(TAG, "Registration failed: ${exception?.message}")
                    callback(false)
                }
            }
    }

    /**
     * שמירת פרטי המשתמש ב-Firestore
     */
    fun userCollection(
        email: String,
        img: String,
        uid: String,
        name: String,
        callback: (Boolean) -> Unit
    ) {
        val db = Firebase.firestore
        val docRef = db.collection("users").document(uid)
        val data = hashMapOf(
            "name" to name,
            "email" to email,
            "photoUrl" to img,
            "uid" to uid,
            "lastLoginTime" to System.currentTimeMillis()
        )
        docRef.set(data).addOnSuccessListener {
            Log.d(TAG, "User saved to Firestore successfully")
            callback(true)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error saving user: ${exception.message}")
            callback(false)
        }
    }

    /**
     * עדכון פרופיל המשתמש עם שם תצוגה
     */
    fun updateUserProfile(user: FirebaseUser, name: String, callback: (Boolean) -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                Log.d(TAG, "User profile updated successfully")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating user profile: ${e.message}")
                callback(false)
            }
    }

    /**
     * קבלת משתמש לפי מזהה
     */
    fun getUserByUid(uid: String, callback: (User?) -> Unit) {
        val db = Firebase.firestore
        val usersCollection = db.collection("users")

        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: ""
                    val email = documentSnapshot.getString("email") ?: ""
                    val photoUrl = documentSnapshot.getString("photoUrl")
                    val lastLoginTime = documentSnapshot.getLong("lastLoginTime") ?: System.currentTimeMillis()

                    val user = User(
                        id = uid,
                        name = name,
                        email = email,
                        photoUrl = photoUrl,
                        isCurrentUser = true,
                        lastLoginTime = lastLoginTime
                    )

                    callback(user)
                } else {
                    Log.d(TAG, "No document found with UID $uid")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user document: ${exception.message}")
                callback(null)
            }
    }

    /**
     * התחברות משתמש קיים
     */
    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login successful")
                    callback(true)
                } else {
                    Log.e(TAG, "Login failed: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    /**
     * שליחת מייל לאיפוס סיסמה
     */
    fun sendPasswordResetEmail(email: String, callback: (Boolean) -> Unit) {
        auth = Firebase.auth
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent")
                    callback(true)
                } else {
                    Log.e(TAG, "Error sending password reset email: ${task.exception?.message}")
                    callback(false)
                }
            }
    }
}