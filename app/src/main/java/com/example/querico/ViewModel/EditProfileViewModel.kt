package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.JoinendModel.JoinedUserModel

class EditProfileViewModel: ViewModel() {
    val userModel = JoinedUserModel()
    fun editProfile(user : UserEntity, password : String, callback: (Boolean) -> Unit){
        userModel.editProfile(user,password,callback)

    }
}