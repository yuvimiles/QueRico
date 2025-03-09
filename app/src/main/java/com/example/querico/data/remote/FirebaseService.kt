package com.example.querico.data.remote

import android.net.Uri
import android.util.Log
import com.example.querico.data.model.Restaurant
import com.example.querico.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseService {
    private val TAG = "FirebaseService"

    // התייחסויות לשירותי Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // אוספים בבסיס הנתונים
    private val usersCollection = firestore.collection("users")
    private val restaurantsCollection = firestore.collection("restaurants")

    // =========== Auth Methods ===========

    /**
     * מחזיר את המזהה של המשתמש הנוכחי, או null אם אין מחובר
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * בודק אם יש משתמש מחובר כרגע
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * מחזיר את המשתמש הנוכחי מ-Firebase Auth
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * התחברות עם אימייל וסיסמה
     */
    fun loginWithEmailAndPassword(email: String, password: String): Task<com.google.firebase.auth.AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    /**
     * יצירת משתמש חדש עם אימייל וסיסמה
     */
    fun createUserWithEmailAndPassword(email: String, password: String): Task<com.google.firebase.auth.AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    /**
     * שליחת אימייל לאיפוס סיסמה
     */
    fun sendPasswordResetEmail(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)
    }

    /**
     * אישור איפוס סיסמה עם קוד אימות
     */
    fun confirmPasswordReset(code: String, newPassword: String): Task<Void> {
        return auth.confirmPasswordReset(code, newPassword)
    }

    /**
     * התנתקות משתמש
     */
    fun logout() {
        auth.signOut()
    }

    // =========== User Methods ===========

    /**
     * שמירת נתוני משתמש ב-Firestore
     */
    fun saveUser(user: User): Task<Void> {
        return usersCollection.document(user.id).set(user)
    }

    /**
     * קבלת נתוני משתמש לפי מזהה
     */
    suspend fun getUserById(userId: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val document = usersCollection.document(userId).get().await()
                if (document.exists()) {
                    document.toObject(User::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user: ${e.message}")
                null
            }
        }
    }

    /**
     * קבלת המשתמש הנוכחי מ-Firestore
     */
    suspend fun getCurrentUserData(): User? {
        val userId = getCurrentUserId() ?: return null
        return getUserById(userId)
    }

    /**
     * עדכון פרטי משתמש ב-Firestore
     */
    fun updateUser(updates: Map<String, Any>, userId: String = getCurrentUserId() ?: ""): Task<Void> {
        return usersCollection.document(userId).update(updates)
    }

    // =========== Restaurant Methods ===========

    /**
     * קבלת כל המסעדות מסודרות לפי זמן יצירה
     */
    suspend fun getRestaurants(limit: Long = 20, lastDocument: DocumentSnapshot? = null): Pair<List<Restaurant>, DocumentSnapshot?> {
        return withContext(Dispatchers.IO) {
            try {
                var query = restaurantsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit)

                if (lastDocument != null) {
                    query = query.startAfter(lastDocument)
                }

                val querySnapshot = query.get().await()
                val restaurants = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Restaurant::class.java)
                }

                val lastVisible = if (querySnapshot.documents.isNotEmpty()) {
                    querySnapshot.documents.last()
                } else null

                Pair(restaurants, lastVisible)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting restaurants: ${e.message}")
                Pair(emptyList(), null)
            }
        }
    }

    /**
     * קבלת מסעדה לפי מזהה
     */
    suspend fun getRestaurantById(id: String): Restaurant? {
        return withContext(Dispatchers.IO) {
            try {
                val document = restaurantsCollection.document(id).get().await()
                if (document.exists()) {
                    document.toObject(Restaurant::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting restaurant: ${e.message}")
                null
            }
        }
    }

    /**
     * קבלת מסעדות של משתמש ספציפי
     */
    suspend fun getRestaurantsByUserId(userId: String): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = restaurantsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Restaurant::class.java)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user restaurants: ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * קבלת מסעדות לפי מרחק מנקודה מסוימת
     */
    suspend fun getRestaurantsByLocation(latitude: Double, longitude: Double, radiusKm: Double): List<Restaurant> {
        // חישוב פשוט של מרחק. בסביבת ייצור אמיתית אפשר להשתמש ב-GeoFirestore
        return withContext(Dispatchers.IO) {
            try {
                val allRestaurants = restaurantsCollection.get().await()
                    .documents.mapNotNull { it.toObject(Restaurant::class.java) }

                allRestaurants.filter { restaurant ->
                    val lat = restaurant.latitude ?: return@filter false
                    val lng = restaurant.longitude ?: return@filter false

                    val distance = calculateDistance(latitude, longitude, lat, lng)
                    distance <= radiusKm
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting restaurants by location: ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * יצירת מסעדה חדשה
     */
    suspend fun createRestaurant(restaurant: Restaurant): String {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = if (restaurant.id.isNotEmpty()) {
                    restaurantsCollection.document(restaurant.id)
                } else {
                    restaurantsCollection.document()
                }

                val newRestaurant = if (restaurant.id.isEmpty()) {
                    restaurant.copy(id = docRef.id)
                } else {
                    restaurant
                }

                docRef.set(newRestaurant).await()
                docRef.id
            } catch (e: Exception) {
                Log.e(TAG, "Error creating restaurant: ${e.message}")
                throw e
            }
        }
    }

    /**
     * עדכון מסעדה קיימת
     */
    suspend fun updateRestaurant(restaurantId: String, updates: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                restaurantsCollection.document(restaurantId).update(updates).await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error updating restaurant: ${e.message}")
                false
            }
        }
    }

    /**
     * מחיקת מסעדה
     */
    suspend fun deleteRestaurant(restaurantId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                restaurantsCollection.document(restaurantId).delete().await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting restaurant: ${e.message}")
                false
            }
        }
    }

    /**
     * עדכון סטטוס מועדף
     */
    suspend fun updateBookmarkStatus(restaurantId: String, isBookmarked: Boolean): Boolean {
        return updateRestaurant(restaurantId, mapOf("isBookmarked" to isBookmarked))
    }

    // =========== Storage Methods ===========

    /**
     * העלאת תמונה ל-Firebase Storage
     */
    fun uploadImage(imageUri: Uri, path: String = "images/${UUID.randomUUID()}"): Task<Uri> {
        val storageRef = storage.reference.child(path)
        return storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                storageRef.downloadUrl
            }
    }

    /**
     * מחיקת תמונה מ-Firebase Storage
     */
    fun deleteImage(imageUrl: String): Task<Void> {
        return storage.getReferenceFromUrl(imageUrl).delete()
    }

    // =========== Helper Methods ===========

    /**
     * חישוב מרחק בק"מ בין שתי נקודות על פני כדור הארץ
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadiusKm * c
    }
}