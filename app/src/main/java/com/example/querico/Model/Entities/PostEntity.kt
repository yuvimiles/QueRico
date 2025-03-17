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
    var uid: String
) : Serializable {
    fun fromMap(map: Map<String?, Any?>) {
        img = map["image"].toString()
        restaurantName = map["restaurantName"].toString()
        content = map["content"].toString()
        location = map["location"].toString()
        uid = map["uid"].toString()
    }
}