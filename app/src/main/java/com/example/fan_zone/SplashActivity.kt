package com.example.fan_zone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fan_zone.models.FirebaseModel

class SplashActivity : AppCompatActivity() {
    private val firebaseModel = FirebaseModel.shared

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = firebaseModel.getCurrentUser()

        val intent = if (user != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, AuthActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
