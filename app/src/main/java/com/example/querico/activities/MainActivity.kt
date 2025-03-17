package com.example.querico.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.querico.Fragments.MapFragment
import com.example.querico.Model.JoiendModel.JoinedPostModel
import com.example.querico.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        setUpNav()
    }
    private fun setUpNav(){

        //get instance bottom nav
        val bottomNav : BottomNavigationView = findViewById(R.id.bottom_navigation)

        // set up the navigation controller
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_navhost_frag) as NavHostFragment
        val navController = navHostFragment.navController

        //react to button push in the bottom navigation using the controller
        bottomNav.setOnItemSelectedListener {
            when(it.itemId){

                R.id.profile_fragment -> {
                    navController.navigate(R.id.action_global_profileFragment)
                }
                R.id.upload_a_post_fragment -> {
                    navController.navigate(R.id.action_global_UploadAPostFragment)
                }
                R.id.my_post_fragment -> {
                    navController.navigate(R.id.action_global_myUploadsFragment)
                }
                R.id.map_fragment-> {
                    navController.navigate(R.id.action_global_mapFragment)
                }
            }

            true
        }
    }
}