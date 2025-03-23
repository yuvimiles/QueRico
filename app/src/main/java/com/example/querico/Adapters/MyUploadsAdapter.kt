package com.example.querico.Adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R
import com.example.querico.ViewModel.MyUploadViewModel
import com.google.android.material.button.MaterialButton


class MyUploadsAdapter(private val viewModel: MyUploadViewModel, activity: FragmentActivity?) :
    RecyclerView.Adapter<MyUploadsAdapter.MyUploadsViewHolder>() {

    private var posts = emptyList<PostEntity>()

    val navHostFragment: NavHostFragment = activity?.supportFragmentManager
        ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
    val navController = navHostFragment.navController

    inner class MyUploadsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_btn)
        private val editButton: MaterialButton = itemView.findViewById(R.id.edit_btn)

        private val image: ImageView = itemView.findViewById(R.id.my_post_image)
        private val restaurantNameTextView: TextView = itemView.findViewById(R.id.my_post_restaurant_name)
        private val contentTextView: TextView = itemView.findViewById(R.id.my_post_content)
        private val locationTextView: TextView = itemView.findViewById(R.id.my_post_location)

        // צריך להוסיף RatingBar לlayout של my_post_card.xml
        private val ratingBar: RatingBar? = itemView.findViewById(R.id.my_post_rating)

        fun bind(post: PostEntity) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(post.img)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(image)

            // Set other post details with correct mapping
            restaurantNameTextView.text = post.restaurantName
            locationTextView.text = post.location

            // Extract rating and content
            var rating = 0f
            var displayContent = post.content

            if (post.content.startsWith("Rating:")) {
                val contentParts = post.content.split("\n\n", limit = 2)
                if (contentParts.size > 1) {
                    // Extract rating value
                    val ratingText = contentParts[0].replace("Rating:", "").trim()
                    try {
                        rating = ratingText.toFloat()
                        // Set just the content part without rating text
                        displayContent = contentParts[1]
                    } catch (e: NumberFormatException) {
                        // If rating conversion fails, keep original content
                    }
                }
            }

            // Set the rating in the RatingBar
            ratingBar?.rating = rating

            // Display clean content (without "Rating: X.X" text)
            contentTextView.text = displayContent

            // Set click listener for delete button
            deleteButton.setOnClickListener {
                val postForDeletion = PostEntity(
                    post.id,
                    post.restaurantName,
                    post.img,
                    post.content,
                    post.location,
                    post.uid
                )

                // Call the ViewModel's deletePost function with the post object
                viewModel.deletePost(postForDeletion)
            }

            // Set up listener for edit button
            editButton.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("post", post)
                navController.navigate(R.id.action_global_editPostFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyUploadsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_post_card, parent, false)
        return MyUploadsViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyUploadsViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newPosts: List<PostEntity>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}