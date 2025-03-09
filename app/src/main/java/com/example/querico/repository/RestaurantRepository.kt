package com.example.querico.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.querico.data.local.RestaurantDao
import com.example.querico.data.model.Restaurant
import com.example.querico.data.remote.FirebaseService
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository למסעדות - שכבת ביניים בין מקורות המידע השונים
 */
class RestaurantRepository(
    private val restaurantDao: RestaurantDao,
    private val firebaseService: FirebaseService
) {
    private val TAG = "RestaurantRepository"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // LiveData לכל המסעדות מהמסד המקומי
    val allRestaurants: LiveData<List<Restaurant>> = restaurantDao.getAllRestaurants()

    // מצביע למסמך האחרון שנטען מ-Firestore (לטעינה מדורגת)
    private var lastLoadedDocument: DocumentSnapshot? = null

    // האם יש עוד נתונים לטעון מהשרת
    private var hasMoreData = true

    // מצב טעינה נוכחי
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // הודעות שגיאה
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * רענון נתונים מהשרת
     */
    suspend fun refreshData() {
        if (_isLoading.value == true) return

        _isLoading.postValue(true)
        try {
            // איפוס מצביעי הטעינה המדורגת
            lastLoadedDocument = null
            hasMoreData = true

            // קבלת נתונים מהשרת - שימוש בפרמטרים הנכונים
            val (restaurants, lastDoc) = firebaseService.getRestaurants(20, lastLoadedDocument)

            // שמירה במסד המקומי
            withContext(Dispatchers.IO) {
                restaurantDao.insertAllRestaurants(restaurants)
            }

            // עדכון מצב הטעינה המדורגת
            lastLoadedDocument = lastDoc
            hasMoreData = restaurants.isNotEmpty()

            _errorMessage.postValue(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing data: ${e.message}")
            _errorMessage.postValue("Failed to refresh data: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }

    /**
     * טעינת עוד נתונים (טעינה מדורגת)
     * @return האם נטענו נתונים נוספים בהצלחה
     */
    suspend fun loadMoreData(): Boolean {
        if (_isLoading.value == true || !hasMoreData) return false

        _isLoading.postValue(true)
        return try {
            // קבלת עוד נתונים מהשרת - שימוש בפרמטרים הנכונים
            val (restaurants, lastDoc) = firebaseService.getRestaurants(20, lastLoadedDocument)

            if (restaurants.isNotEmpty()) {
                // שמירה במסד המקומי
                withContext(Dispatchers.IO) {
                    restaurantDao.insertAllRestaurants(restaurants)
                }

                // עדכון מצב הטעינה המדורגת
                lastLoadedDocument = lastDoc
                hasMoreData = restaurants.isNotEmpty()

                _errorMessage.postValue(null)
                true
            } else {
                hasMoreData = false
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading more data: ${e.message}")
            _errorMessage.postValue("Failed to load more data: ${e.message}")
            false
        } finally {
            _isLoading.postValue(false)
        }
    }

    /**
     * קבלת מסעדה לפי מזהה
     * @param id מזהה המסעדה
     */
    fun getRestaurantById(id: String): LiveData<Restaurant> {
        // הפעלת פונקציית suspend מתוך coroutine
        coroutineScope.launch {
            refreshRestaurantById(id)
        }
        return restaurantDao.getRestaurantById(id)
    }

    /**
     * רענון מסעדה ספציפית מהשרת
     * @param id מזהה המסעדה
     */
    private suspend fun refreshRestaurantById(id: String) {
        try {
            val restaurant = firebaseService.getRestaurantById(id)
            restaurant?.let {
                restaurantDao.insertRestaurant(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing restaurant by id: ${e.message}")
        }
    }

    /**
     * קבלת המסעדות של משתמש ספציפי
     * @param userId מזהה המשתמש
     */
    fun getUserRestaurants(userId: String): LiveData<List<Restaurant>> {
        // הפעלת פונקציית suspend מתוך coroutine
        coroutineScope.launch {
            refreshUserRestaurants(userId)
        }
        return restaurantDao.getUserRestaurants(userId)
    }

    /**
     * רענון המסעדות של משתמש ספציפי מהשרת
     * @param userId מזהה המשתמש
     */
    private suspend fun refreshUserRestaurants(userId: String) {
        try {
            val restaurants = firebaseService.getRestaurantsByUserId(userId)
            withContext(Dispatchers.IO) {
                restaurantDao.insertAllRestaurants(restaurants)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user restaurants: ${e.message}")
        }
    }

    /**
     * יצירת מסעדה חדשה
     */
    suspend fun createRestaurant(
        name: String,
        location: String,
        description: String,
        rating: Float,
        imageUri: Uri?,
        userId: String,
        userName: String,
        userPhotoUrl: String?
    ): Restaurant {
        try {
            // וידוא תקינות הקלט
            require(name.isNotBlank()) { "Restaurant name cannot be empty" }
            require(location.isNotBlank()) { "Location cannot be empty" }

            // העלאת תמונה אם יש
            val imageUrl = if (imageUri != null) {
                try {
                    // גישה חלופית ללא await - נשתמש בקולבקים
                    var resultUrl = ""
                    val uploadTask = firebaseService.uploadImage(imageUri)

                    // המתנה לסיום העלאה במקרה הפשוט
                    val taskResult = uploadTask.addOnSuccessListener { uri ->
                        resultUrl = uri.toString()
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to upload image: ${e.message}")
                    }

                    // המתנה פשוטה עד לסיום (בהמשך נעבור לפתרון מתקדם יותר)
                    while (!taskResult.isComplete) {
                        Thread.sleep(100)
                    }

                    resultUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload image: ${e.message}")
                    "" // URL ריק אם ההעלאה נכשלה
                }
            } else ""

            // יצירת אובייקט המסעדה
            val restaurant = Restaurant(
                id = "", // יתמלא אוטומטית ע"י Firebase
                name = name,
                location = location,
                description = description,
                rating = rating,
                reviewer = userName,
                reviewCount = "0 posts",
                imageUrl = imageUrl,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )

            // שמירה ב-Firebase
            val savedRestaurantId = firebaseService.createRestaurant(restaurant)

            // עדכון המזהה שהתקבל מ-Firebase
            val savedRestaurant = restaurant.copy(id = savedRestaurantId)

            // שמירה במסד המקומי
            restaurantDao.insertRestaurant(savedRestaurant)

            return savedRestaurant
        } catch (e: Exception) {
            Log.e(TAG, "Error creating restaurant: ${e.message}")
            throw e
        }
    }

    /**
     * עדכון מסעדה קיימת
     */
    suspend fun updateRestaurant(restaurant: Restaurant, imageUri: Uri?): Restaurant {
        try {
            // העלאת תמונה חדשה אם יש
            val imageUrl = if (imageUri != null) {
                try {
                    // גישה חלופית ללא await
                    var resultUrl = restaurant.imageUrl // שימור התמונה הקיימת כברירת מחדל
                    val uploadTask = firebaseService.uploadImage(imageUri)

                    val taskResult = uploadTask.addOnSuccessListener { uri ->
                        resultUrl = uri.toString()
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to upload new image: ${e.message}")
                    }

                    // המתנה פשוטה
                    while (!taskResult.isComplete) {
                        Thread.sleep(100)
                    }

                    resultUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload new image: ${e.message}")
                    restaurant.imageUrl
                }
            } else restaurant.imageUrl

            // עדכון אובייקט המסעדה
            val updatedRestaurant = restaurant.copy(
                imageUrl = imageUrl,
                lastSyncedTimestamp = System.currentTimeMillis()
            )

            // עדכון ב-Firebase - שימוש ב-Map כפי שנדרש בפונקציה
            try {
                val restaurantMap = mapOf(
                    "name" to updatedRestaurant.name,
                    "location" to updatedRestaurant.location,
                    "description" to updatedRestaurant.description,
                    "rating" to updatedRestaurant.rating,
                    "imageUrl" to updatedRestaurant.imageUrl,
                    "lastSyncedTimestamp" to updatedRestaurant.lastSyncedTimestamp
                )
                firebaseService.updateRestaurant(updatedRestaurant.id, restaurantMap)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update restaurant in Firebase: ${e.message}")
            }

            // עדכון במסד המקומי
            restaurantDao.updateRestaurant(updatedRestaurant)

            return updatedRestaurant
        } catch (e: Exception) {
            Log.e(TAG, "Error updating restaurant: ${e.message}")
            throw e
        }
    }

    /**
     * מחיקת מסעדה
     */
    suspend fun deleteRestaurant(id: String) {
        try {
            // מחיקה מ-Firebase
            try {
                firebaseService.deleteRestaurant(id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete restaurant from Firebase: ${e.message}")
            }

            // מחיקה מהמסד המקומי
            restaurantDao.deleteRestaurantById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting restaurant: ${e.message}")
            throw e
        }
    }

    /**
     * עדכון סטטוס המועדף של מסעדה
     */
    suspend fun toggleBookmark(restaurantId: String, isBookmarked: Boolean) {
        try {
            // עדכון במסד המקומי
            restaurantDao.updateBookmarkStatus(restaurantId, isBookmarked)

            // עדכון בשרת
            try {
                firebaseService.updateBookmarkStatus(restaurantId, isBookmarked)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update bookmark status in Firebase: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling bookmark: ${e.message}")
            throw e
        }
    }

    /**
     * חיפוש מסעדות לפי טקסט חיפוש
     */
    fun searchRestaurants(query: String): LiveData<List<Restaurant>> {
        return restaurantDao.searchRestaurants(query)
    }

    /**
     * קבלת מסעדות מסומנות כמועדפות
     */
    fun getBookmarkedRestaurants(): LiveData<List<Restaurant>> {
        return restaurantDao.getBookmarkedRestaurants()
    }

    /**
     * קבלת מסעדות לפי דירוג מינימלי
     */
    fun getRestaurantsByMinRating(minRating: Float): LiveData<List<Restaurant>> {
        return restaurantDao.getRestaurantsByMinRating(minRating)
    }
}