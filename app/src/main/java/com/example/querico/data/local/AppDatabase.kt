package com.example.querico.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.querico.data.model.Restaurant
import com.example.querico.data.model.User
import com.example.querico.RicoApplication

/**
 * בסיס הנתונים המקומי של האפליקציה המבוסס על Room.
 * מכיל את כל הטבלאות המקומיות וחושף את ה-DAO לגישה אליהן.
 */
@Database(
    entities = [
        Restaurant::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * מחזיר את ה-DAO לגישה לטבלת המסעדות
     */
    abstract fun restaurantDao(): RestaurantDao

    /**
     * מחזיר את ה-DAO לגישה לטבלת המשתמשים
     */
    abstract fun userDao(): UserDao

    companion object {
        // מקרה יחיד של מסד הנתונים, משותף בין כל התהליכים
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * מחזיר את המקרה היחיד של מסד הנתונים, ויוצר אותו אם עדיין לא קיים.
         *
         * @param context הקונטקסט של האפליקציה לגישה למערכת הקבצים
         * @return מקרה של מסד הנתונים
         */
        fun getDatabase(context: Context): AppDatabase {
            // אם המופע קיים כבר, החזר אותו מיד
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            // אחרת, צור אותו בבלוק מסונכרן (למניעת race conditions)
            synchronized(this) {
                // בדוק שוב אם המופע נוצר בינתיים על ידי thread אחר
                val instance = INSTANCE
                if (instance != null) {
                    return instance
                }

                // צור מופע חדש של מסד הנתונים
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rico_database"
                )
                    // אפשר מיגרציה הרסנית כאשר גרסה חדשה אינה תואמת
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = db
                return db
            }
        }
    }
}