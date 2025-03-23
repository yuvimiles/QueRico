package com.example.querico.Fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R
import com.example.querico.ViewModel.EditPostViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage

class EditPostFragment : Fragment() {
    private lateinit var post: PostEntity
    private lateinit var editPostViewModel: EditPostViewModel
    private lateinit var navController: NavController

    // UI Components
    private lateinit var restaurantNameEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var contentEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var postImageView: ImageView
    private lateinit var saveButton: Button

    // Image handling
    private var imageUri: Uri? = null
    private lateinit var imageUrlRef: String

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Upload the image to Firebase Storage
            uploadImage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        // Get the post from arguments
        post = arguments?.getSerializable("post") as PostEntity

        // Initialize UI components
        postImageView = view.findViewById(R.id.edit_post_image)
        restaurantNameEditText = view.findViewById(R.id.edit_post_restaurant_name_input_text)
        locationEditText = view.findViewById(R.id.edit_post_location_input_text)
        contentEditText = view.findViewById(R.id.edit_post_content)
        ratingBar = view.findViewById(R.id.edit_rating_bar)

        // Set initial image URL from post
        imageUrlRef = post.img

        // Load restaurant image using Glide
        Glide.with(requireContext())
            .load(post.img)
            .placeholder(R.drawable.ic_placeholder_image)
            .error(R.drawable.ic_placeholder_image)
            .into(postImageView)

        // Parse and set content/rating
        extractContentAndRating()

        // Set other post data
        restaurantNameEditText.setText(post.restaurantName)
        locationEditText.setText(post.location)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        editPostViewModel = ViewModelProvider(this)[EditPostViewModel::class.java]

        // Set up navigation controller
        val navHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        navController = navHostFragment.navController

        // Set click listener for image upload
        postImageView.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Find and setup save button
        saveButton = view.findViewById(R.id.save_post_changes_button)
        saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun extractContentAndRating() {
        // Extract rating and content
        val content = post.content
        var rating = 0f
        var actualContent = ""

        // Check if content contains rating information
        if (content.startsWith("Rating:")) {
            val lines = content.split("\n\n", limit = 2)
            if (lines.size > 1) {
                // Extract rating value
                val ratingText = lines[0].replace("Rating:", "").trim()
                try {
                    rating = ratingText.toFloat()
                    // Set just the content part without rating
                    actualContent = lines[1]
                } catch (e: NumberFormatException) {
                    // If rating conversion fails, set the whole content
                    actualContent = content
                }
            } else {
                actualContent = content
            }
        } else {
            actualContent = content
        }

        // Set content and rating to UI
        contentEditText.setText(actualContent)
        ratingBar.rating = rating
    }

    private fun saveChanges() {
        try {
            val restaurantName = restaurantNameEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()
            val rating = ratingBar.rating.toString()

            // Logging for debug
            Log.d("EditPostDebug", "Saving changes:")
            Log.d("EditPostDebug", "Restaurant name: $restaurantName")
            Log.d("EditPostDebug", "Location: $location")
            Log.d("EditPostDebug", "Content: $content")
            Log.d("EditPostDebug", "Rating: $rating")

            // Validate input
            if (restaurantName.isEmpty() || location.isEmpty() || content.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return
            }

            // Include rating in the content
            val contentWithRating = "Rating: $rating\n\n$content"

            // Create updated post entity with explicit parameters
            val updatedPost = PostEntity(
                id = post.id,
                restaurantName = restaurantName,
                img = imageUrlRef,
                content = contentWithRating,
                location = location,
                uid = post.uid
            )

            // Call the ViewModel to update the post
            editPostViewModel.editPost(updatedPost) { isSuccessful ->
                if (isSuccessful) {
                    Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back or to my uploads
                    navController.navigate(R.id.action_global_myUploadsFragment)
                } else {
                    Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("EditPostDebug", "Error saving changes: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage() {
        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("restaurant_images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrlRef = downloadUri.toString()

                    // Load the image into the ImageView
                    Glide.with(requireContext())
                        .load(imageUrlRef)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(postImageView)

                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    // Handle failed download URL retrieval
                    Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                // Handle failed upload
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}