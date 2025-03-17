package com.example.querico.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.querico.Model.Entities.UserEntity
import com.example.querico.R
import com.example.querico.ViewModel.ProfileViewModel
import com.example.querico.activities.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize ViewModel
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Get current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            // Initialize views
            profileImageView = view.findViewById(R.id.profile_image_view)
            nameTextView = view.findViewById(R.id.profile_user_name)
            emailTextView = view.findViewById(R.id.profile_email)

            // Load user data
            profileViewModel.getUserByUid(uid) { userEntity ->
                if (userEntity != null) {
                    // Load profile image
                    Glide.with(requireContext())
                        .load(userEntity.profileImg)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(profileImageView)

                    // Set user info
                    nameTextView.text = userEntity.name
                    emailTextView.text = userEntity.email
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get navigation controller
        val navHostFragment: NavHostFragment = activity?.supportFragmentManager
            ?.findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up edit profile button
        val editButton: MaterialButton = view.findViewById(R.id.edit_profile_button)
        editButton.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                profileViewModel.getUserByUid(currentUser.uid) { userEntity ->
                    if (userEntity != null) {
                        // Create bundle with user data
                        val user = UserEntity(
                            currentUser.uid,
                            userEntity.name,
                            userEntity.profileImg,
                            userEntity.email
                        )
                        val bundle = Bundle()
                        bundle.putSerializable("user", user)

                        // Navigate to edit profile fragment
                        navController.navigate(R.id.action_global_editProfileFragment, bundle)
                    }
                }
            }
        }

        // Set up my posts button
        val myPostsButton: Button = view.findViewById(R.id.my_posts_button)
        myPostsButton.setOnClickListener {
            navController.navigate(R.id.action_global_myUploadsFragment)
        }

        // Set up logout button
        val logoutButton = view.findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            auth.signOut()
            val loginIntent = Intent(context, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            activity?.finish()
        }
    }
}