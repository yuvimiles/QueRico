package com.example.querico.Model.ModelRoom.Model

import androidx.lifecycle.LiveData
import com.example.querico.RicoApplication
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.ModelRoom.AppDB
import com.example.querico.Model.ModelRoom.Dao.PostDao
import java.util.LinkedList


class PostModel {
    fun getAllPosts(): List<PostEntity> {
        return AppDB.getInstance().postDao().getAllPosts()
    }

    fun insertPost(post: PostEntity) {
        val db = AppDB.getInstance().postDao().insertPost(post)
    }
    fun deletePost(post: PostEntity){
        return AppDB.getInstance().postDao().deletePost(post)
    }
    fun getPostsByUid(uid: String) : List<PostEntity> {
        return AppDB.getInstance().postDao().getPostsByUserId(uid)
    }
    fun updatePost(post: PostEntity){
        return AppDB.getInstance().postDao().updatePost(post)
    }
    fun getAllPostsNewestFirst(): List<PostEntity> {
        return AppDB.getInstance().postDao().getAllPostsNewestFirst()
    }

}