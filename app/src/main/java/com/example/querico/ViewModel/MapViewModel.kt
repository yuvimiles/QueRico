package com.example.querico.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoinedModel.JoinedPostModel

class MapViewModel : ViewModel() {
    private val postsModel = JoinedPostModel()

    // Define LiveData to hold the list of posts
    private lateinit var _posts: LiveData<List<PostEntity>>
    val posts: LiveData<List<PostEntity>> get() = _posts

    // Call this function to fetch posts
    fun fetchPosts() {
        _posts = postsModel.getAllPosts()
    }
}