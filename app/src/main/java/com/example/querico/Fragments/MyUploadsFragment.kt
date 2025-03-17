package com.example.querico.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.querico.Adapters.MyUploadsAdapter
import com.example.querico.R
import com.example.querico.ViewModel.MyUploadViewModel
import com.google.firebase.auth.FirebaseAuth

class MyUploadsFragment : Fragment() {
    private lateinit var myUploadsViewModel: MyUploadViewModel
    private lateinit var myAdapter: MyUploadsAdapter
    private lateinit var recyclerView: RecyclerView
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_uploads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ViewModel instance
        myUploadsViewModel = ViewModelProvider(this)[MyUploadViewModel::class.java]

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        myAdapter = MyUploadsAdapter(myUploadsViewModel, activity)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Get current user ID
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            // Fetch user-specific posts
            fetchUserPosts(uid)

            // Observe user posts
            observeUserPosts()
        }
    }

    private fun observeUserPosts() {
        myUploadsViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            myAdapter.submitList(posts)
        }
    }

    private fun fetchUserPosts(uid: String) {
        // Call the getUserPosts function to start observing the LiveData
        myUploadsViewModel.getUserPosts(uid)
    }
}