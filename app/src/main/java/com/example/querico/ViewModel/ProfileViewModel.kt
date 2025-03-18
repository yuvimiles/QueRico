package com.example.querico.ViewModel

import androidx.lifecycle.ViewModel
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.JoinendModel.JoinedUserModel

class ProfileViewModel: ViewModel() {
    val usersModel = JoinedUserModel()

    fun getUserByUid(uid :String,  callback: (UserEntity)-> Unit){
        return usersModel.getUserByUid(uid, callback)
    }
}