package com.example.querico.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.querico.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.querico.Model.ModelFB.PostFB

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                R.id.feed_fragment -> {
                    navController.navigate(R.id.action_global_feedFragment)
                }
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