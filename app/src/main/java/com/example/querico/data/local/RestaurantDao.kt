package com.example.querico.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.querico.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

/**
 * ממשק גישה לנתונים (DAO) עבור טבלת המסעדות.
 * מספק פונקציות לביצוע פעולות על נתוני המסעדות בבסיס הנתונים המקומי.
 */
@Dao
interface RestaurantDao {
    /**
     * מחזיר את כל המסעדות מסודרות לפי זמן יצירה יורד (חדש לישן)
     */
    @Query("SELECT * FROM restaurants ORDER BY timestamp DESC")
    fun getAllRestaurants(): LiveData<List<Restaurant>>

    /**
     * מחזיר את כל המסעדות כ-Flow (עבור Coroutines)
     */
    @Query("SELECT * FROM restaurants ORDER BY timestamp DESC")
    fun getAllRestaurantsFlow(): Flow<List<Restaurant>>

    /**
     * מחזיר מסעדה לפי מזהה
     * @param id מזהה המסעדה
     */
    @Query("SELECT * FROM restaurants WHERE id = :id")
    fun getRestaurantById(id: String): LiveData<Restaurant>

    /**
     * מחזיר מסעדה לפי מזהה באופן סינכרוני
     * @param id מזהה המסעדה
     */
    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantByIdSync(id: String): Restaurant?

    /**
     * מחזיר את כל המסעדות של משתמש מסוים
     * @param userId מזהה המשתמש
     */
    @Query("SELECT * FROM restaurants WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserRestaurants(userId: String): LiveData<List<Restaurant>>

    /**
     * מוסיף מסעדה חדשה או מעדכן קיימת
     * @param restaurant אובייקט המסעדה
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant)

    /**
     * מוסיף רשימת מסעדות
     * @param restaurants רשימת מסעדות
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRestaurants(restaurants: List<Restaurant>)

    /**
     * מעדכן מסעדה קיימת
     * @param restaurant אובייקט המסעדה
     */
    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)

    /**
     * מוחק מסעדה
     * @param restaurant אובייקט המסעדה
     */
    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)

    /**
     * מוחק מסעדה לפי מזהה
     * @param id מזהה המסעדה
     */
    @Query("DELETE FROM restaurants WHERE id = :id")
    suspend fun deleteRestaurantById(id: String)

    /**
     * מקבל דף מסעדות (לטעינה מדורגת)
     * @param limit מספר המסעדות בדף
     * @param offset מיקום ההתחלה
     */
    @Query("SELECT * FROM restaurants ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getRestaurantsPaginated(limit: Int, offset: Int): List<Restaurant>

    /**
     * מחזיר מסעדות לפי דירוג מינימלי
     * @param minRating דירוג מינימלי
     */
    @Query("SELECT * FROM restaurants WHERE rating >= :minRating ORDER BY rating DESC")
    fun getRestaurantsByMinRating(minRating: Float): LiveData<List<Restaurant>>

    /**
     * מחזיר מסעדות שמסומנות כמועדפות
     */
    @Query("SELECT * FROM restaurants WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedRestaurants(): LiveData<List<Restaurant>>

    /**
     * מחפש מסעדות לפי שם או מיקום
     * @param query מחרוזת חיפוש
     */
    @Query("SELECT * FROM restaurants WHERE name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRestaurants(query: String): LiveData<List<Restaurant>>

    /**
     * עדכון סטטוס מועדף של מסעדה
     * @param id מזהה המסעדה
     * @param isBookmarked האם מסומנת כמועדפת
     */
    @Query("UPDATE restaurants SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)

    /**
     * עדכון זמן הסנכרון האחרון של המסעדה
     * @param id מזהה המסעדה
     * @param timestamp זמן הסנכרון
     */
    @Query("UPDATE restaurants SET lastSyncedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateSyncTimestamp(id: String, timestamp: Long)

    /**
     * מחזיר את מספר המסעדות בבסיס הנתונים
     */
    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun getRestaurantCount(): Int

    /**
     * מוחק את כל המסעדות מהמסד נתונים המקומי
     * שימושי לפעולות ניקוי וריענון מלא של הנתונים
     */
    @Query("DELETE FROM restaurants")
    suspend fun deleteAllRestaurants()
}