package com.example.querico.ViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoinendModel.JoinedPostModel
import com.google.firebase.auth.FirebaseAuth

class MyUploadViewModel : ViewModel() {
    private val postsModel = JoinedPostModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // MutableLiveData to hold posts by UID data
    private lateinit var _userPosts: MutableLiveData<List<PostEntity>>;
    val userPosts: LiveData<List<PostEntity>> get() = _userPosts
    private val user= auth.currentUser
    val uid= user!!.uid

    // Method to set user posts in the LiveData
    fun getUserPosts(uid: String) {
        this._userPosts = postsModel.getPostsByUid(uid)
    }

    fun deletePost(post: PostEntity) {
        postsModel.deletePost(post) {isSuccessful ->
            if (isSuccessful) {
                val postsList = this._userPosts.value?.filter{ item -> item.id != post.id }
                this._userPosts.postValue(postsList)
            } else {
                println("error deleting the post")
                // TODO: Toast a message
            }
        }
    }

}