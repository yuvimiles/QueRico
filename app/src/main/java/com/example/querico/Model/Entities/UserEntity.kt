package com.example.querico.Model.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    var uid: String,

    var name: String,

    @ColumnInfo(name = "profile_img")
    var profileImg: String,

    var email: String

): Serializable
