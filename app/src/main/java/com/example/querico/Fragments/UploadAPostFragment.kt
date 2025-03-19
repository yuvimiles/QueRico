package com.example.querico.Fragments


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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
import com.example.querico.ViewModel.UploadPostViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class UploadAPostFragment : Fragment() {
    private lateinit var uploadPostViewModel: UploadPostViewModel
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var restaurantNameEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var contentEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var addImageButton: ImageButton
    private lateinit var createPostButton: Button
    private lateinit var progressBar : ProgressBar
    // Image handling
    private var imageUri: Uri? = null
    private lateinit var imageUrlRef: String

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            addImageButton.setImageURI(it)
            // Upload the image to Firebase Storage
            //uploadImage()
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

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize ViewModel
        uploadPostViewModel = ViewModelProvider(this)[UploadPostViewModel::class.java]

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
        createPostButton = view.findViewById(R.id.new_post_button)
        progressBar = view.findViewById(R.id.progressBar)

        // Set default image URL (replace with your default image)
        imageUrlRef = "https://firebasestorage.googleapis.com/v0/b/querico-6dd96.firebasestorage.app/o/profile_default_img.png?alt=media&token=56948c77-4804-4dcf-9654-0957083bfe61"
        // Set click listener for image upload button
        addImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Set click listener for create post button
        createPostButton.setOnClickListener {
            val restaurantName = restaurantNameEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()
            val rating = ratingBar.rating.toString()

            // Include rating in the content


            // Validate the input
            if (validate(restaurantName, location, content)) {
                // Get current user ID
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    // Create post entity
                    uploadImage()
                } else {
                    // User not logged in
                    Toast.makeText(context, "You need to be logged in to create a post", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show validation error
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPostWithImage(){
        val restaurantName = restaurantNameEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()
        val rating = ratingBar.rating.toString()
        val contentWithRating = "Rating: $rating\n\n$content"
        val userId = auth.currentUser?.uid
        if(userId != null){
            val post = PostEntity("", restaurantName, imageUrlRef, contentWithRating, location, userId)

            // Upload the post
            uploadPostViewModel.uploadPost(post) { isSuccessful ->
                if (isSuccessful) {
                    Toast.makeText(context, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to the feed or to my uploads
                    navController.navigate(R.id.myUploadsFragment) // Replace with your actual navigation action
                } else {
                    Toast.makeText(context, "Failed to upload post", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun validate(restaurantName: String, location: String, content: String): Boolean {
        return restaurantName.isNotEmpty() && location.isNotEmpty() && content.isNotEmpty()
    }

    private fun uploadImage() {
        // Show loading indicator or disable buttons if needed
//        createPostButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        imageUri?.let {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("restaurant_images/${System.currentTimeMillis()}.jpg")

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded image
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUrlRef = downloadUri.toString()
                    uploadPostWithImage()
                }.addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    // Handle failed download URL retrieval
                    Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    createPostButton.isEnabled = true
                }
            }.addOnFailureListener { e ->
                // Handle failed upload
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                createPostButton.isEnabled = true
            }
        }
    }
}