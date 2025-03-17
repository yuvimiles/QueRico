package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.JoiendModel.JoinedPostModel
import com.example.querico.Model.JoiendModel.JoinedUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterUserViewModel: ViewModel() {
    val UserModel = JoinedUserModel()

    fun register(user : UserEntity, password: String,  callback: (Boolean) -> Unit){
        UserModel.register(user, password, callback)

    }
}