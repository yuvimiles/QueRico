package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoinendModel.JoinedPostModel

class UploadPostViewModel : ViewModel() {
    val postsModel = JoinedPostModel.instance


    fun uploadPost(post: PostEntity, callback: (Boolean) -> Unit) {
        postsModel.uploadPost(post, callback)
    }
}