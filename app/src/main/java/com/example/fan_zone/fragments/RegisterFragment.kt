package com.example.fan_zone.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fan_zone.MainActivity
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentRegisterBinding
import com.example.fan_zone.repositories.UserRepository

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listener for the "Sign Up" button
        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate inputs
            if (validateInput(fullName, email, password)) {
                createUser(fullName, email, password)
            }
        }

        // Navigate to Login screen when "Sign In" is clicked
        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validateInput(fullName: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(fullName) -> {
                binding.etEmail.error = "Full name is required"
                false
            }

            TextUtils.isEmpty(email) -> {
                binding.etEmail.error = "Email is required"
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Invalid email address"
                false
            }

            TextUtils.isEmpty(password) -> {
                binding.etPassword.error = "Password is required"
                false
            }

            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }

            else -> true
        }
    }

    private fun createUser(fullName: String, email: String, password: String) {
        updateIsLoading(true)
        userRepository.createUser(
            fullName,
            email,
            password,
            onSuccess = {
                Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                updateIsLoading(false)
            },
            onFailure = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                updateIsLoading(false)
            }
        )
    }

    private fun updateIsLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
