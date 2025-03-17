package com.example.querico.Model.ModelFB

import com.example.querico.Model.Entities.PostEntity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.LinkedList

class PostFB {
    companion object {
        val COLLECTION_NAME: String = "posts"
    }
    val db = Firebase.firestore
    //get all post from firebase
    fun getAllPosts(callback: (List<PostEntity>) -> Unit) {
        val query: Query = db.collection(COLLECTION_NAME)

        query.get().addOnCompleteListener{snapshot->
            if(snapshot.isSuccessful){
                val list = LinkedList<PostEntity>()
                val doc = snapshot.result

                for(postMap in doc){
                    val post = PostEntity(postMap.id, "", "", "", "", "")
                    post.fromMap(postMap.data)


                    list.add(post)
                }

                callback(list)
            }
        }
    }

    //get all the posts by user id
    fun getPostsByUid(uid: String, callback: (List<PostEntity>) -> Unit) {
        val query: Query = db.collection(COLLECTION_NAME)
            .whereEqualTo("uid", uid)
        query.get().addOnCompleteListener { snapshot ->
            if (snapshot.isSuccessful) {
                val list = mutableListOf<PostEntity>()

                for (document in snapshot.result!!) {
                    val post = PostEntity(document.id, "", "", "", "", "")
                    post.fromMap(document.data)
                    list.add(post)
                }

                callback(list)
            } else {
                val exception = snapshot.exception
                println("Error fetching posts for UID: $uid. Exception: $exception")
                // Handle the case where the task was not successful
                callback(emptyList())
            }
        }
    }

    fun deletePostFromFirebase(post: PostEntity, callback: (Boolean) -> Unit) {
        val id = post.id
        db.collection("posts").document(id)
            .delete()
            .addOnSuccessListener {
                callback(true) // Successful deletion from Firebase
            }
            .addOnFailureListener { exception ->
                callback(false) // Handle deletion failure
                println("Error deleting post with ID: $id. Exception: $exception")
            }
    }

    fun updatePost(post: PostEntity, callback: (Boolean) -> Unit){
        val db = Firebase.firestore
        val postDocRef = db.collection(COLLECTION_NAME).document(post.id)
        val updatedPostData = hashMapOf(
            "restaurantName" to post.restaurantName,
            "content" to post.content,
            "location" to post.location,
            "image" to post.img,
            "uid" to post.uid
        )
        postDocRef.update(updatedPostData as Map<String, Any>)
            .addOnSuccessListener {
                println("Post updated successfully")
                callback(true)
            }
            .addOnFailureListener { exception ->
                println("Error updating post: ${exception.message}")
                callback(false)
            }

    }

    fun uploadPost(post: PostEntity, callback: (Boolean) -> Unit){
        val db = Firebase.firestore
        val docRef = db.collection("posts").document()
        val data = hashMapOf(
            "restaurantName" to post.restaurantName,
            "content" to post.content,
            "location" to post.location,
            "uid" to post.uid,
            "image" to post.img
        )
        docRef.set(data).addOnSuccessListener {
            println("Post uploaded successfully")
            callback(true)
        }.addOnFailureListener { exception ->
            println("Error uploading post: ${exception.message}")
            callback(false)
        }

    }


}