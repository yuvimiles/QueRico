package com.example.querico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.querico.R
import com.example.querico.data.model.Restaurant

class PostFragment : Fragment() {
    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantName: TextView
    private lateinit var restaurantLocation: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingBy: TextView
    private lateinit var description: TextView
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupClickListeners()
        loadRestaurantData()
    }

    private fun initViews(view: View) {
        restaurantImage = view.findViewById(R.id.ivRestaurant)
        restaurantName = view.findViewById(R.id.tvRestaurantName)
        restaurantLocation = view.findViewById(R.id.tvLocation)
        ratingBar = view.findViewById(R.id.ratingBar)
        ratingBy = view.findViewById(R.id.tvRatingBy)
        description = view.findViewById(R.id.tvDescription)
        backButton = view.findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadRestaurantData() {
        arguments?.let { args ->
            restaurantName.text = args.getString(ARG_NAME, "")
            restaurantLocation.text = args.getString(ARG_LOCATION, "")
            ratingBar.rating = args.getFloat(ARG_RATING, 0f)
            ratingBy.text = "Rating by ${args.getString(ARG_REVIEWER, "")}"
            description.text = args.getString(ARG_DESCRIPTION, "")

            // טיפול בתמונה
            if (args.containsKey(ARG_IMAGE_RES_ID) && args.getInt(ARG_IMAGE_RES_ID) != 0) {
                // מזהה משאב
                val imageResId = args.getInt(ARG_IMAGE_RES_ID)
                restaurantImage.setImageResource(imageResId)
            } else {
                // URL של תמונה
                val imageUrl = args.getString(ARG_IMAGE_URL)
                if (!imageUrl.isNullOrEmpty()) {
                    // טען תמונה באמצעות Glide
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_image) // הוסף משאב placeholder אמיתי
                        .error(R.drawable.ic_error_image) // הוסף משאב error אמיתי
                        .into(restaurantImage)
                } else {
                    // אם אין URL ואין משאב תמונה, הצג תמונת ברירת מחדל
                    restaurantImage.setImageResource(R.drawable.ic_placeholder_image) // הוסף משאב ברירת מחדל אמיתי
                }
            }
        }
    }

    companion object {
        private const val ARG_NAME = "arg_name"
        private const val ARG_LOCATION = "arg_location"
        private const val ARG_RATING = "arg_rating"
        private const val ARG_REVIEWER = "arg_reviewer"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_IMAGE_URL = "arg_image_url"
        private const val ARG_IMAGE_RES_ID = "arg_image_res_id"

        fun newInstance(restaurant: Restaurant) = PostFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NAME, restaurant.name)
                putString(ARG_LOCATION, restaurant.location)
                putFloat(ARG_RATING, restaurant.rating)
                putString(ARG_REVIEWER, restaurant.reviewer)
                putString(ARG_DESCRIPTION, restaurant.description)

                // טיפול בתמונה - הפרדה בין URL לבין משאב מקומי
                if (restaurant.imageResourceId != 0) {
                    // אם יש מזהה משאב תמונה (drawable resource ID)
                    putInt(ARG_IMAGE_RES_ID, restaurant.imageResourceId)
                } else {
                    // אחרת שמור את ה-URL של התמונה
                    putString(ARG_IMAGE_URL, restaurant.imageUrl)
                }
            }
        }
    }
}