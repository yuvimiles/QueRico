package com.example.querico.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.querico.Adapters.PostAdapter
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.JoinedModel.JoinedPostModel
import com.example.querico.R
import com.example.querico.ViewModel.FeedViewModel

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.feed_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshPosts()
        }

        // Initialize ViewModel
        feedViewModel = ViewModelProvider(this).get(FeedViewModel::class.java)

        // Initialize adapter
        postAdapter = PostAdapter { post ->
            onPostClick(post)
        }

        recyclerView.adapter = postAdapter

        // Observe posts data
        observePosts()

        // Load initial posts
        feedViewModel.fetchPosts()

        return rootView
    }

    private fun refreshPosts() {
        feedViewModel.fetchPosts()
    }

    private fun observePosts() {
        JoinedPostModel.instance.getAllPosts().observe(viewLifecycleOwner){posts ->
            if (posts != null) {
                postAdapter.updatePosts(posts)
                swipeRefreshLayout.isRefreshing = false
            } else {
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        }
        feedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    private fun onPostClick(post: PostEntity) {
        val navHostFragment: NavHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        val navController = navHostFragment.navController

        val bundle = Bundle()
        bundle.putSerializable("post", post)
        navController.navigate(R.id.action_global_singlePostCardFragment, bundle)
    }
}