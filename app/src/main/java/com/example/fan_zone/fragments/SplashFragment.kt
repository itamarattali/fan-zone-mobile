package com.example.fan_zone.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fan_zone.R
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Add a delay to show splash screen for a moment
        Handler(Looper.getMainLooper()).postDelayed({
            checkUser()
        }, 2000) // 2 seconds delay
    }

    private fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, navigate to profile
            findNavController().navigate(R.id.action_splashFragment_to_profileFragment)
        } else {
            // No user is signed in, navigate to login
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
} 