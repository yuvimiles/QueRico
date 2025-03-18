package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.JoinendModel.JoinedUserModel

class RegisterUserViewModel: ViewModel() {
    val UserModel = JoinedUserModel()

    fun register(user : UserEntity, password: String,  callback: (Boolean) -> Unit){
        UserModel.register(user, password, callback)

    }
}