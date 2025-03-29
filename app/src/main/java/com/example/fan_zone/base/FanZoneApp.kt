package com.example.fan_zone.base

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class FanZoneApp : Application() {
    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
        FirebaseApp.initializeApp(this)
    }
}
