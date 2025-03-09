package com.example.querico.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.querico.R
import com.example.querico.data.model.Restaurant
import com.squareup.picasso.Picasso

class RestaurantAdapter : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private var restaurants = mutableListOf<Restaurant>()
    private var onItemClickListener: ((Restaurant) -> Unit)? = null

    fun setOnItemClickListener(listener: (Restaurant) -> Unit) {
        onItemClickListener = listener
    }

    fun addItem(restaurant: Restaurant) {
        restaurants.add(restaurant)
        notifyItemInserted(restaurants.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < restaurants.size) {
            restaurants.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateItem(position: Int, restaurant: Restaurant) {
        if (position >= 0 && position < restaurants.size) {
            restaurants[position] = restaurant
            notifyItemChanged(position)
        }
    }

    fun updateList(newList: List<Restaurant>) {
        val oldList = restaurants.toList()
        restaurants.clear()
        restaurants.addAll(newList)

        if (oldList.isEmpty()) {
            notifyItemRangeInserted(0, newList.size)
            return
        }

        if (newList.size > oldList.size) {
            notifyItemRangeInserted(oldList.size, newList.size - oldList.size)
        } else if (newList.size < oldList.size) {
            notifyItemRangeRemoved(newList.size, oldList.size - newList.size)
        }

        val minSize = minOf(oldList.size, newList.size)
        for (i in 0 until minSize) {
            if (oldList[i] != newList[i]) {
                notifyItemChanged(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant, onItemClickListener)
    }

    override fun getItemCount() = restaurants.size

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val locationTextView: TextView = itemView.findViewById(R.id.tvLocation)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val restaurantImage: ImageView = itemView.findViewById(R.id.ivRestaurant)
        private val reviewerTextView: TextView = itemView.findViewById(R.id.tvReviewer)
        private val reviewCountTextView: TextView = itemView.findViewById(R.id.tvReviewCount)

        fun bind(restaurant: Restaurant, listener: ((Restaurant) -> Unit)?) {
            nameTextView.text = restaurant.name
            locationTextView.text = restaurant.location
            ratingBar.rating = restaurant.rating
            reviewerTextView.text = restaurant.reviewer
            reviewCountTextView.text = restaurant.reviewCount

            // טעינת תמונה - בדיקה אם יש URL מרוחק או משאב מקומי
            if (restaurant.imageUrl.isNotEmpty()) {
                // יש לנו URL מרוחק - משתמשים ב-Picasso
                Picasso.get()
                    .load(restaurant.imageUrl)
                    .placeholder(R.drawable.restaurant1)  // תמונת ברירת מחדל בזמן טעינה
                    .error(R.drawable.restaurant1)        // תמונה שתוצג במקרה של שגיאה
                    .into(restaurantImage)
            } else if (restaurant.imageResourceId != 0) {
                // יש לנו משאב מקומי
                restaurantImage.setImageResource(restaurant.imageResourceId)
            } else {
                // אין תמונה - מציגים תמונת ברירת מחדל
                restaurantImage.setImageResource(R.drawable.restaurant1)
            }

            // הוספת onClick למרכיב
            itemView.setOnClickListener {
                listener?.invoke(restaurant)
            }
        }
    }
}