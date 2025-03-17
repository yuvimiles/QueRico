package com.example.querico.Model.ModelRoom.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.querico.Model.Entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity);

    @Transaction
    @Query("SELECT * FROM users where uid = :uid")
    fun getUserById(uid: String): UserEntity;

    @Update
    fun updateUser(user: UserEntity)
}