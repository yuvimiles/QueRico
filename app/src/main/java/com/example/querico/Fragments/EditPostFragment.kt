package com.example.querico.Fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var addImageButton: ImageButton
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
        return inflater.inflate(R.layout.fragment_upload_a_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the post from arguments
        post = arguments?.getSerializable("post") as PostEntity

        // Initialize ViewModel
        editPostViewModel = ViewModelProvider(this)[EditPostViewModel::class.java]

        // Set up navigation controller
        val navHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize UI components
        restaurantNameEditText = view.findViewById(R.id.new_post_restaurant_name_input_text)
        locationEditText = view.findViewById(R.id.new_post_location_input_text)
        contentEditText = view.findViewById(R.id.new_post_content)
        ratingBar = view.findViewById(R.id.ratingBar)
        addImageButton = view.findViewById(R.id.register_add_image)
        saveButton = view.findViewById(R.id.new_post_button)

        // Change button text to "Save Changes"
        saveButton.text = "Save Changes"

        // Set initial image URL from post
        imageUrlRef = post.img

        // Fill form with post data
        restaurantNameEditText.setText(post.restaurantName)
        locationEditText.setText(post.location)

        // Extract rating and content
        val content = post.content
        var rating = 0f

        // Check if content contains rating information
        if (content.startsWith("Rating:")) {
            val lines = content.split("\n\n", limit = 2)
            if (lines.size > 1) {
                // Extract rating value
                val ratingText = lines[0].replace("Rating:", "").trim()
                try {
                    rating = ratingText.toFloat()
                    // Set just the content part without rating
                    contentEditText.setText(lines[1])
                } catch (e: NumberFormatException) {
                    // If rating conversion fails, set the whole content
                    contentEditText.setText(content)
                }
            } else {
                contentEditText.setText(content)
            }
        } else {
            contentEditText.setText(content)
        }

        // Set rating
        ratingBar.rating = rating

        // Load restaurant image
        context?.let {
            Glide.with(it)
                .load(post.img)
                .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        addImageButton.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // Do nothing
                    }
                })
        }

        // Set click listener for image upload button
        addImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Set click listener for save button
        saveButton.setOnClickListener {
            val restaurantName = restaurantNameEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()
            val rating = ratingBar.rating.toString()

            // Include rating in the content
            val contentWithRating = "Rating: $rating\n\n$content"

            // Validate the input
            if (validate(restaurantName, location, content)) {
                // Create updated post entity
                val updatedPost = PostEntity(
                    post.id,
                    restaurantName,
                    imageUrlRef,
                    contentWithRating,
                    location,
                    post.uid
                )

                // Call the ViewModel to update the post
                editPostViewModel.editPost(updatedPost) { isSuccessful ->
                    if (isSuccessful) {
                        Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                        // Navigate back or to my uploads
                        navController.navigate(R.id.action_global_myUploadsFragment) // Replace with your actual navigation action
                    } else {
                        Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Show validation error
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validate(restaurantName: String, location: String, content: String): Boolean {
        return restaurantName.isNotEmpty() && location.isNotEmpty() && content.isNotEmpty()
    }

    private fun uploadImage() {
        // Show loading indicator or disable buttons if needed
        saveButton.isEnabled = false

        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("restaurant_images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrlRef = downloadUri.toString()

                    // Load the image into the button background or show a preview
                    context?.let { ctx ->
                        Glide.with(ctx)
                            .load(imageUrlRef)
                            .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                                override fun onResourceReady(
                                    resource: android.graphics.drawable.Drawable,
                                    transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                                ) {
                                    addImageButton.setImageDrawable(resource)
                                }

                                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                                    // Do nothing
                                }
                            })
                    }

                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }.addOnFailureListener { e ->
                    // Handle failed download URL retrieval
                    Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
            }.addOnFailureListener { e ->
                // Handle failed upload
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            }
        }
    }
}