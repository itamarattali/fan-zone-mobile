package com.example.fan_zone

import android.app.Application
import com.google.firebase.FirebaseApp

class FanZoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
