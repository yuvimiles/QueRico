package com.example.querico.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.querico.data.model.Restaurant
import com.example.querico.repository.RestaurantRepository
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class RestaurantViewModel(
    private val repository: RestaurantRepository
) : ViewModel() {

    // LiveData למצב טעינה ושגיאות - מגיעים ישירות מהרפוזיטורי
    val isLoading: LiveData<Boolean> = repository.isLoading
    val errorMessage: LiveData<String?> = repository.errorMessage

    // LiveData לכל המסעדות
    val restaurants: LiveData<List<Restaurant>> = repository.allRestaurants

    // LiveData למסעדה נוכחית
    private val _currentRestaurant = MutableLiveData<Restaurant>()
    val currentRestaurant: LiveData<Restaurant> = _currentRestaurant

    // LiveData לתוצאת פעולות (הצלחה/כישלון)
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> = _operationSuccess

    init {
        // טעינת נתונים ראשונית
        refreshData()
    }

    /**
     * רענון נתונים מהשרת
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                repository.refreshData()
            } catch (e: Exception) {
                _operationSuccess.value = false
            }
        }
    }

    /**
     * טעינת עוד נתונים (טעינה מדורגת)
     */
    fun loadMoreData() {
        viewModelScope.launch {
            try {
                repository.loadMoreData()
            } catch (e: Exception) {
                // כישלון הטעינה מטופל בתוך הרפוזיטורי
            }
        }
    }

    /**
     * קבלת מסעדה לפי מזהה
     */
    fun getRestaurantById(id: String): LiveData<Restaurant> {
        return repository.getRestaurantById(id)
    }

    /**
     * בחירת מסעדה נוכחית
     */
    fun selectRestaurant(restaurant: Restaurant) {
        _currentRestaurant.value = restaurant
    }

    /**
     * קבלת המסעדות של המשתמש הנוכחי
     */
    fun getUserRestaurants(userId: String): LiveData<List<Restaurant>> {
        return repository.getUserRestaurants(userId)
    }

    /**
     * קבלת המסעדות של המשתמש המחובר כעת
     */
    fun getCurrentUserRestaurants(): LiveData<List<Restaurant>>? {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return currentUserId?.let { repository.getUserRestaurants(it) }
    }

    /**
     * יצירת מסעדה חדשה
     */
    fun createRestaurant(
        name: String,
        location: String,
        description: String,
        rating: Float,
        imageUri: Uri?
    ): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userId = currentUser?.uid ?: ""
                val userName = currentUser?.displayName ?: "Anonymous"
                val userPhotoUrl = currentUser?.photoUrl

                // קריאה לרפוזיטורי ליצירת מסעדה
                repository.createRestaurant(
                    name = name,
                    location = location,
                    description = description,
                    rating = rating,
                    imageUri = imageUri,
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = userPhotoUrl?.toString()
                )

                result.value = true
                refreshData() // רענון הנתונים לאחר יצירה
            } catch (e: Exception) {
                result.value = false
            }
        }

        return result
    }

    /**
     * עדכון מסעדה קיימת
     */
    fun updateRestaurant(restaurant: Restaurant, newImageUri: Uri?): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                repository.updateRestaurant(restaurant, newImageUri)
                result.value = true
                refreshData() // רענון הנתונים לאחר עדכון
            } catch (e: Exception) {
                result.value = false
            }
        }

        return result
    }

    /**
     * מחיקת מסעדה
     */
    fun deleteRestaurant(id: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                repository.deleteRestaurant(id)
                result.value = true
                refreshData() // רענון הנתונים לאחר מחיקה
            } catch (e: Exception) {
                result.value = false
            }
        }

        return result
    }

    /**
     * שינוי סטטוס המועדף של מסעדה
     */
    fun toggleBookmark(restaurantId: String, isCurrentlyBookmarked: Boolean): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                repository.toggleBookmark(restaurantId, !isCurrentlyBookmarked)
                result.value = true
            } catch (e: Exception) {
                result.value = false
            }
        }

        return result
    }

    /**
     * חיפוש מסעדות לפי טקסט חיפוש
     */
    fun searchRestaurants(query: String): LiveData<List<Restaurant>> {
        return repository.searchRestaurants(query)
    }

    /**
     * קבלת מסעדות מסומנות כמועדפות
     */
    fun getBookmarkedRestaurants(): LiveData<List<Restaurant>> {
        return repository.getBookmarkedRestaurants()
    }

    /**
     * קבלת מסעדות לפי דירוג מינימלי
     */
    fun getRestaurantsByMinRating(minRating: Float): LiveData<List<Restaurant>> {
        return repository.getRestaurantsByMinRating(minRating)
    }
}