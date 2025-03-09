package com.example.querico.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ישות המייצגת מסעדה/פוסט באפליקציה.
 */
@Entity(tableName = "restaurants")
@Parcelize
data class Restaurant(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val rating: Float = 0f,
    val reviewer: String = "",
    val reviewCount: String = "0 posts",
    val description: String = "",
    val imageUrl: String = "",
    val imageResourceId: Int = 0,
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isBookmarked: Boolean = false,
    val lastSyncedTimestamp: Long = System.currentTimeMillis() // הוספנו שדה זה
) : Parcelable