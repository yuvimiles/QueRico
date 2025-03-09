package com.example.querico.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * מחלקת המרה עבור Room Database.
 * מאפשרת המרה בין טיפוסי נתונים מורכבים לטיפוסים פשוטים שנתמכים ב-SQLite.
 */
class Converters {
    /**
     * ממיר ערך Long לאובייקט Date
     * @param value ערך זמן במילישניות
     * @return אובייקט Date או null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * ממיר אובייקט Date לערך Long
     * @param date אובייקט Date
     * @return ערך זמן במילישניות או null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * ממיר ערך Double למחרוזת
     * שימושי עבור מיקום (latitude/longitude)
     * @param value ערך Double
     * @return מחרוזת מייצגת או null
     */
    @TypeConverter
    fun fromDouble(value: Double?): String? {
        return value?.toString()
    }

    /**
     * ממיר מחרוזת לערך Double
     * @param value מחרוזת מייצגת
     * @return ערך Double או null אם הפורמט לא תקין
     */
    @TypeConverter
    fun toDouble(value: String?): Double? {
        return value?.toDoubleOrNull()
    }

    /**
     * ממיר רשימת מחרוזות למחרוזת אחת מופרדת בפסיקים
     * @param values רשימת מחרוזות
     * @return מחרוזת אחת עם ערכים מופרדים בפסיקים
     */
    @TypeConverter
    fun fromStringList(values: List<String>?): String? {
        return values?.joinToString(",")
    }

    /**
     * ממיר מחרוזת אחת מופרדת בפסיקים לרשימת מחרוזות
     * @param value מחרוזת מופרדת בפסיקים
     * @return רשימת מחרוזות
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotEmpty() }
    }

    /**
     * ממיר ערך Boolean למספר שלם
     * @param value ערך בוליאני
     * @return 1 אם true, 0 אם false
     */
    @TypeConverter
    fun fromBoolean(value: Boolean?): Int? {
        return if (value == true) 1 else 0
    }

    /**
     * ממיר מספר שלם לערך Boolean
     * @param value ערך מספרי
     * @return true אם הערך > 0, אחרת false
     */
    @TypeConverter
    fun toBoolean(value: Int?): Boolean? {
        return value != null && value > 0
    }
}