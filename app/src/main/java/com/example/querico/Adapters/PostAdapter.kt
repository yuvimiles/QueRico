package com.example.querico.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.ModelRoom.Model.UserModel
import com.example.querico.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostAdapter(private val onPostClickListener: (PostEntity) -> Unit) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts: List<PostEntity> = ArrayList()
    private val userModel = UserModel()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view, userModel)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)

        holder.itemView.setOnClickListener {
            onPostClickListener(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<PostEntity>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    class PostViewHolder(itemView: View, private val userModel: UserModel) : RecyclerView.ViewHolder(itemView) {
        private val userProfileImageView: ImageView = itemView.findViewById(R.id.profile_image_view)
        private val userNameTextView: TextView = itemView.findViewById(R.id.profile_user_name)
        private val restaurantNameTextView: TextView = itemView.findViewById(R.id.restaurant_name)
        private val contentTextView: TextView = itemView.findViewById(R.id.post_content)
        private val locationTextView: TextView = itemView.findViewById(R.id.post_location)
        private val postImageView: ImageView = itemView.findViewById(R.id.post_image)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.single_post_rating)
        private val viewMoreButton: MaterialButton = itemView.findViewById(R.id.view_more_button)

        fun bind(post: PostEntity) {
            restaurantNameTextView.text = post.restaurantName
            contentTextView.text = post.content
            locationTextView.text = post.location

            ratingBar.rating = 0.0f

            // טען את תמונת הפוסט באמצעות Glide אם קיימת
            if (post.img.isNotEmpty() && post.img != "null") {
                Glide.with(itemView.context)
                    .load(post.img)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(postImageView)
                postImageView.visibility = View.VISIBLE
            } else {
                postImageView.visibility = View.GONE
            }

            val uid = post.uid
            if (uid.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val user = userModel.getUserById(uid)

                        withContext(Dispatchers.Main) {
                            userNameTextView.text = user.name ?: user.email ?: "Unknown User"

                            if (!user.profileImg.isNullOrEmpty()) {
                                Glide.with(itemView.context)
                                    .load(user.profileImg)
                                    .placeholder(R.drawable.ic_placeholder_image)
                                    .error(R.drawable.ic_placeholder_image)
                                    .circleCrop()
                                    .into(userProfileImageView)
                            } else {
                                userProfileImageView.setImageResource(R.drawable.ic_placeholder_image)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            userNameTextView.text = "Unknown User"
                            userProfileImageView.setImageResource(R.drawable.ic_placeholder_image)
                        }
                    }
                }
            } else {
                userNameTextView.text = "Unknown User"
                userProfileImageView.setImageResource(R.drawable.ic_placeholder_image)
            }

            viewMoreButton.setOnClickListener {
                onItemSelected(post)
            }
        }

        private fun onItemSelected(post: PostEntity) {
            itemView.performClick()
        }
    }
}