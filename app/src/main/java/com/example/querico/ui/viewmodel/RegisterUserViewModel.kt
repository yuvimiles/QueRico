package com.example.querico.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.querico.data.firebase.UserFB
import com.example.querico.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterUserViewModel : ViewModel() {
    private val userFB = UserFB()
    private val auth: FirebaseAuth = Firebase.auth

    /**
     * פונקציה לרישום משתמש חדש
     * @param user אובייקט המשתמש שיש לרשום
     * @param password הסיסמה שהוקלדה
     * @param callback פונקציית קולבק שתוזמן עם תוצאת הרישום (הצלחה/כישלון)
     */
    fun register(user: User, password: String, callback: (Boolean) -> Unit) {
        // קודם רושמים את המשתמש ב-Firebase Auth
        userFB.register(user.email, password) { isRegistrationSuccessful ->
            if (isRegistrationSuccessful) {
                // אם הרישום הצליח, שומרים את ה-UID שהתקבל
                val uid = auth.currentUser?.uid

                if (uid != null) {
                    // מעדכנים את פרופיל המשתמש עם השם
                    auth.currentUser?.let { firebaseUser ->
                        userFB.updateUserProfile(firebaseUser, user.name) { isProfileUpdateSuccessful ->
                            // ללא קשר לתוצאת עדכון הפרופיל, מנסים לשמור את נתוני המשתמש ב-Firestore
                            userFB.userCollection(
                                email = user.email,
                                img = user.photoUrl ?: "",
                                uid = uid,
                                name = user.name
                            ) { isUserSaved ->
                                // מחזירים את תוצאת התהליך הכולל
                                callback(isUserSaved)
                            }
                        }
                    }
                } else {
                    // אם אין UID, מחזירים כישלון
                    callback(false)
                }
            } else {
                // אם הרישום ב-Auth נכשל, מחזירים כישלון
                callback(false)
            }
        }
    }
}