package com.example.querico.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.ModelFB.PostFB

class FeedViewModel : ViewModel() {

    private val postRepository = PostFB()

    private val _posts = MutableLiveData<List<PostEntity>>()
    val posts: LiveData<List<PostEntity>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch all posts from Firebase
    fun fetchPosts() {
        _isLoading.value = true
        postRepository.getAllPosts { postsList ->
            _posts.value = postsList.sortedByDescending { it.id } // Sort by newest first (assuming ID has timestamp info)
            _isLoading.value = false
        }
    }

    // Fetch posts by user ID
    fun fetchPostsByUserId(userId: String) {
        _isLoading.value = true
        postRepository.getPostsByUid(userId) { postsList ->
            _posts.value = postsList.sortedByDescending { it.id }
            _isLoading.value = false
        }
    }
}