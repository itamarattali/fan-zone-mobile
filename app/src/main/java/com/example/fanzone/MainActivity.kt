package com.example.fanzone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fanzone.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // No need to explicitly find the NavHostFragment
        // It’s automatically managed if you have app:defaultNavHost="true" in XML.
    }
}
