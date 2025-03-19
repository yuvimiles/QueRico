package com.example.querico.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.R
import okhttp3.*
import java.io.IOException
import org.json.JSONObject

class SinglePostCardFragment : Fragment() {

    private lateinit var post: PostEntity
    private lateinit var postImageView: ImageView
    private lateinit var restaurantNameTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var cuisineInfoTextView: TextView
    private lateinit var backButton: View



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
        cuisineInfoTextView = view.findViewById(R.id.single_post_cuisine_info)
        backButton = view.findViewById(R.id.back_to_feed_button)

        backButton.setOnClickListener {
            // Navigate back to the previous screen (feed)
            requireActivity().onBackPressed()
        }

        // Set the values to the views
        Glide.with(requireContext())
            .load(post.img)
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

        // Fetch cuisine information for the restaurant
        fetchCuisineInfo(post.restaurantName)

        return view
    }

    private fun fetchCuisineInfo(restaurantName: String) {
        val client = OkHttpClient()

        // Replace spaces with dashes for the API request
        val formattedName = restaurantName.replace(" ", "-").toLowerCase()

        val request = Request.Builder()
            .url("https://restaurants-api.p.rapidapi.com/restaurants?query=$formattedName")
            .get()
            .addHeader("X-RapidAPI-Key", "YOUR_RAPID_API_KEY") // Replace with your API key
            .addHeader("X-RapidAPI-Host", "restaurants-api.p.rapidapi.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    cuisineInfoTextView.text = "Could not fetch cuisine information"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseData = response.body?.string()

                    // Update UI with the response data
                    activity?.runOnUiThread {
                        val cuisineInfo = formatCuisineInfo(responseData)
                        cuisineInfoTextView.text = cuisineInfo
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        cuisineInfoTextView.text = "Error processing cuisine information"
                    }
                }
            }
        })
    }


    private fun formatCuisineInfo(responseData: String?): String {
        if (responseData.isNullOrEmpty()) return "No cuisine information available"

        try {
            val jsonObject = JSONObject(responseData)

            // Check if the response contains restaurant data
            if (jsonObject.has("results") && jsonObject.getJSONArray("results").length() > 0) {
                val restaurantInfo = jsonObject.getJSONArray("results").getJSONObject(0)

                val cuisineType = if (restaurantInfo.has("cuisine")) {
                    restaurantInfo.getString("cuisine")
                } else {
                    "Unknown cuisine"
                }

                val priceRange = if (restaurantInfo.has("price_range")) {
                    restaurantInfo.getString("price_range")
                } else {
                    "Unknown price range"
                }

                return "Cuisine: $cuisineType\nPrice Range: $priceRange"
            } else {
                return "No detailed cuisine information available for this restaurant"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Could not parse cuisine information"
        }
    }


}