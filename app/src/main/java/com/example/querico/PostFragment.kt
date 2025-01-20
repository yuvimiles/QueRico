package com.example.querico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment

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
            args.getInt(ARG_IMAGE).let { imageRes ->
                restaurantImage.setImageResource(imageRes)
            }
        }
    }

    companion object {
        private const val ARG_NAME = "arg_name"
        private const val ARG_LOCATION = "arg_location"
        private const val ARG_RATING = "arg_rating"
        private const val ARG_REVIEWER = "arg_reviewer"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_IMAGE = "arg_image"

        fun newInstance(restaurant: Restaurant) = PostFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_NAME, restaurant.name)
                putString(ARG_LOCATION, restaurant.location)
                putFloat(ARG_RATING, restaurant.rating)
                putString(ARG_REVIEWER, restaurant.reviewer)
                putString(ARG_DESCRIPTION, "Lorem ipsum dolor sit amet...")
                putInt(ARG_IMAGE, restaurant.imageUrl)
            }
        }
    }
}