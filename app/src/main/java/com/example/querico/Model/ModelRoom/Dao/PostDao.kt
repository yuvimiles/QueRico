package com.example.querico.Model.ModelRoom.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.querico.Model.Entities.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: PostEntity)

    @Update
    fun updatePost(post: PostEntity)

    @Delete
    fun deletePost(post: PostEntity)

    @Query("SELECT * FROM posts WHERE uid = :userId")
    fun getPostsByUserId(userId: String): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsNewestFirst(): List<PostEntity>
}