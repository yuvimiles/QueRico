package com.example.querico.Model.JoinendModel

import com.example.querico.RicoApplication
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.Model.JoinedModel.JoinedPostModel
import com.example.querico.Model.ModelFB.UserFB
import com.example.querico.Model.ModelRoom.Model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinedUserModel {
    companion object {
        var instance: JoinedPostModel = JoinedPostModel()
    }

    private val firbaseModel = UserFB()
    private val modelRoom = UserModel()
    private lateinit var auth: FirebaseAuth

    fun register(user: UserEntity, password: String,callback: (Boolean) -> Unit ){
        auth = Firebase.auth
        firbaseModel.register(user.email,password){ isSuccessful ->
            if(isSuccessful){
                val uid =auth.currentUser?.uid
                firbaseModel.userCollection(user.email,user.profileImg,uid.toString(),user.name){success->
                    if(success){
                        RicoApplication.getExecutorService().execute{
                            if (uid != null) {
                                user.uid=uid
                                modelRoom.insert(user)
                            }
                        }
                    }
                    callback(success)

                }
            }
            callback(isSuccessful)
        }

    }

    fun getUserByUid(uid: String,callback: (UserEntity) -> Unit ){
        firbaseModel.getUserByUid(uid){userEntity ->
            if(userEntity!= null){
                RicoApplication.getExecutorService().execute{
                    modelRoom.getUserById(uid)
                }
                callback(userEntity)
            }
        }
    }

    fun editProfile(user: UserEntity, password: String,callback: (Boolean) -> Unit ){
        firbaseModel.editProfile(user,password){isSuccessful ->
            if(isSuccessful){
                RicoApplication.getExecutorService().execute{
                    modelRoom.updateUser(user)
                }
            }
            callback(isSuccessful)
        }

    }

}