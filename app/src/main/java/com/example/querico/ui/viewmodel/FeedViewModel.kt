package com.example.querico.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.querico.data.local.AppDatabase
import com.example.querico.data.model.Restaurant
import com.example.querico.data.remote.FirebaseService
import com.example.querico.repository.RestaurantRepository
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    // הגדרת רפוזיטורי
    private val repository: RestaurantRepository

    // LiveData לרשימת המסעדות
    val restaurants: LiveData<List<Restaurant>>

    // מידע על סטטוס טעינה
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // הודעות שגיאה
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // אתחול רפוזיטורי
        val database = AppDatabase.getDatabase(application)
        val firebaseService = FirebaseService()
        repository = RestaurantRepository(database.restaurantDao(), firebaseService)

        // קבלת נתונים מה-Room DB באמצעות הרפוזיטורי
        restaurants = repository.allRestaurants

        // טעינת נתונים ראשונית
        refreshData()
    }

    /**
     * רענון נתונים מהשרת
     */
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshData()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * טעינת עוד נתונים (עבור טעינה מדורגת)
     */
    fun loadMoreData() {
        if (_isLoading.value == true) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val hasMore = repository.loadMoreData()
                if (!hasMore) {
                    // הודעה אופציונלית אם אין עוד נתונים
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load more data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * פונקציה לטיפול בסימון/ביטול סימון מסעדה כמועדפת
     */
    fun toggleBookmark(restaurantId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleBookmark(restaurantId, isBookmarked)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update bookmark: ${e.message}"
            }
        }
    }

    /**
     * פונקציה לקבלת המסעדות של המשתמש הנוכחי
     */
    fun getUserRestaurants(userId: String): LiveData<List<Restaurant>> {
        return repository.getUserRestaurants(userId)
    }
}