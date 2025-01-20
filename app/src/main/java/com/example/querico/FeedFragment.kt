package com.example.querico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class FeedFragment : Fragment() {

    private val viewModel: FeedViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private val restaurantAdapter = RestaurantAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        observeViewModel()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rvRestaurants)
        recyclerView.apply {
            adapter = restaurantAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        restaurantAdapter.setOnItemClickListener { restaurant ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostFragment.newInstance(restaurant))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeViewModel() {
        viewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            restaurantAdapter.updateList(restaurants)
        }
    }

    companion object {
        fun newInstance() = FeedFragment()
    }
}