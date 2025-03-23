package com.example.querico.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.querico.API.OpenAIService
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class SinglePostCardFragment : Fragment() {

    private lateinit var post: PostEntity
    private lateinit var postImageView: ImageView
    private lateinit var restaurantNameTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var backButton: View

    // OpenAI related fields
    private lateinit var openAIService: OpenAIService
    private lateinit var aiAnalysisTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_post_card, container, false)

        // Get the PostEntity object from the arguments
        post = arguments?.getSerializable("post") as PostEntity

        // Find the views in the layout
        postImageView = view.findViewById(R.id.single_post_image)
        restaurantNameTextView = view.findViewById(R.id.single_post_restaurant_name)
        contentTextView = view.findViewById(R.id.single_post_content)
        locationTextView = view.findViewById(R.id.single_post_location)
        ratingBar = view.findViewById(R.id.single_post_rating)
        backButton = view.findViewById(R.id.back_to_feed_button)

        // Initialize OpenAI service
        openAIService = OpenAIService()

        // Find the AI analysis TextView
        aiAnalysisTextView = view.findViewById(R.id.single_post_ai_analysis)

        backButton.setOnClickListener {
            // Navigate back to the previous screen (feed)
            requireActivity().onBackPressed()
        }

        // Set the values to the views
        Glide.with(requireContext())
            .load(post.img)
            .error(R.drawable.restaurant1) // תמונת ברירת מחדל מקומית
            .placeholder(R.drawable.placeholder_image) // אנימציית טעינה
            .into(postImageView)

        restaurantNameTextView.text = post.restaurantName
        locationTextView.text = post.location

        // Parse content to extract rating if available
        val content = post.content
        if (content.startsWith("Rating:")) {
            val lines = content.split("\n\n", limit = 2)
            if (lines.size > 1) {
                // Extract rating value
                val ratingText = lines[0].replace("Rating:", "").trim()
                try {
                    ratingBar.rating = ratingText.toFloat()
                    // Set just the content part without rating
                    contentTextView.text = lines[1]
                } catch (e: NumberFormatException) {
                    // If rating conversion fails, set the whole content
                    contentTextView.text = content
                    ratingBar.rating = 0f
                }
            } else {
                contentTextView.text = content
                ratingBar.rating = 0f
            }
        } else {
            contentTextView.text = content
            ratingBar.rating = 0f
        }

        // Analyze post with OpenAI (ChatGPT)
        analyzeRestaurantWithAI()

        return view
    }

    // Function to analyze the restaurant post with ChatGPT
    private fun analyzeRestaurantWithAI() {
        lifecycleScope.launch {
            try {
                // Display loading state
                aiAnalysisTextView.text = "Analyzing restaurant information..."

                // Get analysis from OpenAI
                val analysis = openAIService.analyzeRestaurant(
                    restaurantName = post.restaurantName,
                    content = post.content
                )

                // Display analysis in UI
                aiAnalysisTextView.text = analysis
            } catch (e: Exception) {
                Log.e("SinglePostCard", "Error analyzing restaurant", e)
                aiAnalysisTextView.text = "Could not analyze restaurant information. Please try again later."
            }
        }
    }
}