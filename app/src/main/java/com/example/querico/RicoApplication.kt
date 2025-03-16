package com.example.querico

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class RicoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // אתחול Firebase
        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        FirebaseAuth.getInstance().useAppLanguage()

        // אתחול Picasso
        val picasso = Picasso.Builder(this)
            .indicatorsEnabled(false)
            .loggingEnabled(false)
            .build()

        Picasso.setSingletonInstance(picasso)
    }
}