package com.example.querico.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R

class PostAdapter(private val onPostClickListener: (PostEntity) -> Unit) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts: List<PostEntity> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
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

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantNameTextView: TextView = itemView.findViewById(R.id.restaurant_name)
        private val contentTextView: TextView = itemView.findViewById(R.id.post_content)
        private val locationTextView: TextView = itemView.findViewById(R.id.post_location)
        private val postImageView: ImageView = itemView.findViewById(R.id.post_image)

        fun bind(post: PostEntity) {
            restaurantNameTextView.text = post.restaurantName
            contentTextView.text = post.content
            locationTextView.text = post.location

            // Load image using Glide if the image URL is not empty
            if (post.img.isNotEmpty() && post.img != "null") {
                Glide.with(itemView.context)
                    .load(post.img)
                    .placeholder(R.drawable.placeholder_image) // Add a placeholder image in your drawable folder
                    .error(R.drawable.error_image) // Add an error image in your drawable folder
                    .into(postImageView)
                postImageView.visibility = View.VISIBLE
            } else {
                postImageView.visibility = View.GONE
            }
        }
    }
}