package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoiendModel.JoinedPostModel
import com.google.firebase.auth.FirebaseAuth

class EditPostViewModel: ViewModel() {
    val postsModel = JoinedPostModel()

    fun editPost(post : PostEntity, callback: (Boolean) -> Unit){
        postsModel.editPost(post, callback)
    }
}