package com.example.querico

import android.app.Application
import com.google.firebase.FirebaseApp
import com.squareup.picasso.Picasso

class RicoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // אתחול Firebase
        FirebaseApp.initializeApp(this)

        // אתחול Picasso
        val picasso = Picasso.Builder(this)
            .indicatorsEnabled(false)
            .loggingEnabled(false)
            .build()

        Picasso.setSingletonInstance(picasso)
    }
}