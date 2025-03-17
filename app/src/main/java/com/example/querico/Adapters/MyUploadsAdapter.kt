package com.example.querico.Adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        private val deleteButton: MaterialButton = itemView.findViewById(com.example.querico.R.id.delete_btn)
        private val editButton: MaterialButton = itemView.findViewById(com.example.querico.R.id.edit_btn)

        private val image: ImageView = itemView.findViewById(com.example.querico.R.id.my_post_image)
        private val kindTextView: TextView = itemView.findViewById(com.example.querico.R.id.my_post_content)
        private val ageTextView: TextView = itemView.findViewById(com.example.querico.R.id.my_post_restaurant_name)
        private val locationTextView: TextView =
            itemView.findViewById(com.example.querico.R.id.my_post_location)

        fun bind(post: PostEntity) {
            // Load image using Glide
            Glide.with(itemView.context).load(post.img).into(image)

            // Set other post details
            kindTextView.text = post.restaurantName
            ageTextView.text = post.content
            locationTextView.text = post.location

            // Set click listener for delete button
            deleteButton.setOnClickListener {
                var postForDeletion = PostEntity(
                    post.id, post.img, post.restaurantName, post.content, post.location, post.uid
                )

                // Call the ViewModel's deletePost function with the post object
                viewModel.deletePost(postForDeletion)
            }

            //set up listener for edit button
            editButton.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("post", post)
                navController.navigate(R.id.action_global_editPostFragment, bundle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyUploadsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.example.querico.R.layout.my_post_card, parent, false)
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