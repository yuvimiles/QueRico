plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    // הוספנו את Google services - שים לב שאנחנו משתמשים ב-alias
    id("com.google.gms.google-services")
    // הוספנו את הפלאגינים לתמיכה ב-kapt ו-parcelize
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.example.querico"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.querico"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // הוספנו תמיכה ב-ViewBinding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ספריות Android קיימות
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.maps)

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Room - הוספנו תמיכה בבסיס נתונים מקומי
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    // הוספנו את התלויות הדרושות לפרויקט שלך
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    // Firebase
    implementation ("com.google.firebase:firebase-auth:22.3.1")

    // Google Sign In
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    // Maps - תמיכה במיקום
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Coroutines - לעבודה אסינכרונית
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Picasso - לטעינת תמונות
    implementation("com.squareup.picasso:picasso:2.8")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}