package com.example.fan_zone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        // Navigate to the correct screen
        val intent = if (user != null) {
            Intent(this, MainActivity::class.java) // User is signed in
        } else {
            Intent(this, AuthActivity::class.java) // User is not signed in
        }

        startActivity(intent)
        finish() // Finish SplashActivity so it’s removed from the back stack
    }
}
