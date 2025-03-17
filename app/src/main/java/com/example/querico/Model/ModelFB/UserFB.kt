package com.example.querico.Model.ModelFB


import com.example.querico.Model.Entities.UserEntity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class UserFB {
    private lateinit var auth: FirebaseAuth

    fun register(email: String, password: String, callback: (Boolean) -> Unit) {
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    callback(true)
                } else {
                    // User registration failed
                    val exception = task.exception
                    println("Registration failed: ${exception?.message}")
                    callback(false)
                }

            }

    }

    fun userCollection(
        email: String,
        img: String,
        uid: String,
        name: String,
        callback: (Boolean) -> Unit
    ) {
        val db = Firebase.firestore
        val docRef = db.collection("users").document()
        val data = hashMapOf(
            "name" to name,
            "email" to email,
            "img" to img,
            "uid" to uid
        )
        docRef.set(data).addOnSuccessListener {
            println("Post uploaded successfully")
            callback(true)
        }.addOnFailureListener { exception ->
            println("Error uploading user: ${exception.message}")
            callback(false)
        }
    }

    fun getUserByUid(uid: String, callback: (UserEntity?) -> Unit) {
        val db = Firebase.firestore
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents.first()
                    val name = documentSnapshot.getString("name") ?: ""
                    val email = documentSnapshot.getString("email") ?: ""
                    val profileImg = documentSnapshot.getString("img") ?: ""

                    val userEntity = UserEntity(
                        uid = uid,
                        name = name,
                        profileImg = profileImg,
                        email = email
                    )

                    callback(userEntity)
                } else {
                    println("No document found with UID $uid")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching user document: ${exception.message}")
                callback(null)
            }
    }

    fun editProfile(user: UserEntity, password: String, callback: (Boolean) -> Unit) {
        val db = Firebase.firestore
        val usersCollection = db.collection("users")
        auth = Firebase.auth
        usersCollection.whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents.first()
                    var name = documentSnapshot.getString("name") ?: ""
                    var email = documentSnapshot.getString("email") ?: ""
                    var profileImg = documentSnapshot.getString("img") ?: ""

                    if (name != user.name) {
                        name = user.name
                    }
                    if (email != user.email) {
                        email = user.email
                        val current = auth.currentUser
                        if (current != null) {
                            updateEmail(current, email) { isSuccessful->
                                if(isSuccessful){
                                    println("update email in auth")
                                }
                                else {
                                    println("falied to update email in auth")
                                }

                            }
                        }
                    }
                    if (profileImg != user.profileImg) {
                        profileImg = user.profileImg
                    }

                    val updatedData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "img" to profileImg,
                        "uid" to user.uid
                    )
                    if (password != "") {
                        // Update password and user profile data
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            updatePassword(currentUser, password) { success ->
                                if (success) {
                                    updateUserProfile(
                                        documentSnapshot.id,
                                        updatedData,
                                        callback
                                    )
                                } else {
                                    callback(false)
                                }
                            }
                        } else {
                            // Handle case where user is not authenticated
                            callback(false)
                        }
                    } else {
                        // Update user profile data only
                        updateUserProfile(documentSnapshot.id, updatedData, callback)
                    }
                } else {
                    // Handle case where the user document is not found
                    callback(false)
                }
            }
    }




    private fun updateUserProfile(
        documentId: String,
        updatedData: Map<String, Any>,
        callback: (Boolean) -> Unit
    ) {
        val db = Firebase.firestore
        val userRef = db.collection("users").document(documentId)

        userRef.update(updatedData)
            .addOnSuccessListener {
                // Update successful
                callback(true)
            }
            .addOnFailureListener { exception ->
                // Handle update failure
                callback(false)
            }
    }

    private fun updatePassword(
        currentUser: FirebaseUser,
        newPassword: String,
        callback: (Boolean) -> Unit
    ) {
        currentUser.updatePassword(newPassword)
            .addOnSuccessListener {
                // Password update successful
                callback(true)
            }
            .addOnFailureListener { exception ->
                // Handle password update failure
                callback(false)
            }
    }

    private fun updateEmail(
        currentUser: FirebaseUser,
        newEmail: String,
        callback: (Boolean) -> Unit
    ) {
        currentUser.updateEmail(newEmail)
            .addOnSuccessListener {
                // Email update successful
                callback(true)
            }
            .addOnFailureListener { exception ->
                // Handle email update failure
                callback(false)
            }
    }
}