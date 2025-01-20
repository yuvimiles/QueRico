package com.example.querico

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Set the content view (activity layout)
        setContentView(R.layout.activity_main)

        // Apply window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the MapFragment dynamically into a container
        if (savedInstanceState == null) { // Avoid reloading fragment on configuration changes
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, MapFragment()) // Replace with your actual container ID
                .commit()
        }
    }
}
