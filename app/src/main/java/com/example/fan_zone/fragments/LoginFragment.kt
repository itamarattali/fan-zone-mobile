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
import com.example.fan_zone.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            findNavController().navigate(R.id.profileFragment)
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
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
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        updateIsLoading(true)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                } else {
                    Toast.makeText(
                        context,
                        "Invalid credentials, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                updateIsLoading(false)
            }.addOnFailureListener(requireActivity()) { _ ->
                Toast.makeText(
                    context,
                    "Login failed",
                    Toast.LENGTH_SHORT
                ).show()
                updateIsLoading(false)
            }
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
