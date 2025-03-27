package com.example.querico.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoinedModel.JoinedPostModel
import com.example.querico.Model.ModelFB.PostFB
import com.google.firebase.auth.FirebaseAuth

class FeedViewModel : ViewModel() {

    private val postRepository = PostFB()


    private val postsModel = JoinedPostModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    private val _posts = MutableLiveData<List<PostEntity>>()
    val posts: LiveData<List<PostEntity>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch all posts from Firebase
    fun fetchPosts() {
        _isLoading.value = true

        // _posts.value = postsModel.getAllPosts().value
        postRepository.getAllPosts { postsList ->
            _posts.value = postsList.sortedByDescending { it.id } // Sort by newest first (assuming ID has timestamp info)
            _isLoading.value = false
        }
    }

}