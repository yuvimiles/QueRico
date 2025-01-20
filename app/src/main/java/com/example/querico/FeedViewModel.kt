package com.example.querico
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> = _restaurants

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        val restaurantsList = listOf(
            Restaurant(
                id = "1",
                name = "Ambrosia Hotel & Restaurant",
                location = "Kazi Deiry, Taiger Pass Chittagong",
                rating = 3.0f,
                reviewer = "Justice Life",
                reviewCount = "800+ posts",
                imageUrl = R.drawable.restaurant1
            ),
            Restaurant(
                id = "2",
                name = "Haatkhola",
                location = "Kazi Deiry, Taiger Pass Chittagong",
                rating = 3.5f,
                reviewer = "Matan Ben Sahel",
                reviewCount = "400+ posts",
                imageUrl = R.drawable.restaurant1
            ),
        )

        _restaurants.value = restaurantsList
    }
}