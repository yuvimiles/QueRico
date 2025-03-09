package com.example.querico.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.querico.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * ממשק גישה לנתונים (DAO) עבור טבלת המשתמשים.
 * מספק פונקציות לביצוע פעולות על נתוני המשתמשים בבסיס הנתונים המקומי.
 */
@Dao
interface UserDao {
    /**
     * מחזיר משתמש לפי מזהה
     * @param id מזהה המשתמש
     */
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): LiveData<User>

    /**
     * מחזיר משתמש לפי מזהה באופן סינכרוני
     * @param id מזהה המשתמש
     */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSync(id: String): User?

    /**
     * מחזיר את המשתמש הנוכחי (המחובר)
     */
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): LiveData<User?>

    /**
     * מחזיר את המשתמש הנוכחי (המחובר) באופן סינכרוני
     */
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): User?

    /**
     * מחזיר את המשתמש לפי אימייל
     * @param email אימייל המשתמש
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): LiveData<User?>

    /**
     * מוסיף משתמש חדש או מעדכן קיים
     * @param user אובייקט המשתמש
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * מעדכן משתמש קיים
     * @param user אובייקט המשתמש
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * מוחק משתמש
     * @param user אובייקט המשתמש
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * מוחק משתמש לפי מזהה
     * @param id מזהה המשתמש
     */
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    /**
     * מעדכן את מצב "משתמש נוכחי" למשתמש ספציפי
     * @param id מזהה המשתמש
     * @param isCurrentUser האם זה המשתמש הנוכחי
     */
    @Query("UPDATE users SET isCurrentUser = :isCurrentUser WHERE id = :id")
    suspend fun updateCurrentUserStatus(id: String, isCurrentUser: Boolean)

    /**
     * מבטל את מצב "משתמש נוכחי" לכל המשתמשים
     * שימושי בעת התנתקות
     */
    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUserStatus()

    /**
     * מעדכן את שם המשתמש
     * @param id מזהה המשתמש
     * @param name השם החדש
     */
    @Query("UPDATE users SET name = :name WHERE id = :id")
    suspend fun updateUserName(id: String, name: String)

    /**
     * מעדכן את כתובת תמונת הפרופיל של המשתמש
     * @param id מזהה המשתמש
     * @param photoUrl כתובת התמונה החדשה
     */
    @Query("UPDATE users SET photoUrl = :photoUrl WHERE id = :id")
    suspend fun updateUserPhoto(id: String, photoUrl: String?)

    /**
     * מעדכן את זמן ההתחברות האחרון של המשתמש
     * @param id מזהה המשתמש
     * @param lastLoginTime זמן ההתחברות האחרון
     */
    @Query("UPDATE users SET lastLoginTime = :lastLoginTime WHERE id = :id")
    suspend fun updateLastLoginTime(id: String, lastLoginTime: Long)

    /**
     * מחזיר את כל המשתמשים
     * בד"כ לשימוש אדמיניסטרטיבי בלבד
     */
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): LiveData<List<User>>

    /**
     * מחזיר את מספר המשתמשים בבסיס הנתונים
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}