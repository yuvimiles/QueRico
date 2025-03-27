package com.example.querico.Model.Entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "posts")
data class PostEntity (
    @PrimaryKey()
    var id: String,
    var restaurantName: String,
    var img: String,
    var content: String,
    var location: String,
    var uid: String,
    var timestamp: Long = 0

) : Serializable {
    fun fromMap(map: Map<String?, Any?>) {
        uid = map["uid"].toString()
        img = map["image"].toString()
        restaurantName = map["restaurantName"].toString()
        location = map["location"].toString()
        content = map["content"].toString()

        val timestampValue = map["timestamp"]
        if (timestampValue != null) {
            // בדיקה אם הערך הוא מספר (Long)
            if (timestampValue is Long) {
                timestamp = timestampValue
            }
            // אם זה מחרוזת שניתן להמיר למספר
            else if (timestampValue is String) {
                try {
                    timestamp = timestampValue.toLong()
                } catch (e: NumberFormatException) {
                    timestamp = System.currentTimeMillis() // ברירת מחדל אם ההמרה נכשלה
                }
            }
            // אחרת השתמש בזמן הנוכחי
            else {
                timestamp = System.currentTimeMillis()
            }
        } else {
            // אם אין שדה timestamp במפה, השתמש בזמן הנוכחי
            timestamp = System.currentTimeMillis()
        }
    }
}