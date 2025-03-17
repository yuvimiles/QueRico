package com.example.querico.Model.ModelRoom.Model

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.querico.RicoApplication
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.ModelRoom.AppDB

class UserModel {

    fun insert(user: UserEntity){
        val db = AppDB.getInstance().userDao().insert(user)
    }
    fun getUserById(uid: String): UserEntity {
        return AppDB.getInstance().userDao().getUserById(uid)
    }

    fun updateUser(user: UserEntity){
        return AppDB.getInstance().userDao().updateUser(user)
    }


}