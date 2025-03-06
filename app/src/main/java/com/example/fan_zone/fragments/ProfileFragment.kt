package com.example.fan_zone.fragments

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.fan_zone.AuthActivity
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentProfileBinding
import com.example.fan_zone.models.Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var shouldUpdateProfilePicture = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var model: Model

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding.ivProfilePicture.setImageBitmap(it)
            shouldUpdateProfilePicture = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setupUI()
        loadUserProfile()
    }

    private fun initialize() {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        model = Model.shared
    }

    private fun setupUI() {
        with(binding) {
            btnEdit.setOnClickListener { enterEditMode() }
            btnSave.setOnClickListener {
                firebaseAuth.currentUser?.uid?.let { userId ->
                    saveEditChanges(userId)
                }
            }
            btnChangePicture.setOnClickListener { pickImageFromGallery() }
            btnSignOut.setOnClickListener { signOut() }
        }
    }

    private fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            navigateToAuth()
            return
        }

        showLoading(true)

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document.exists()) {
                    updateUIWithUserData(document)
                } else {
                    showToast("User data not found")
                }
            }
            .addOnFailureListener {
                showToast("Failed to load profile")
                showLoading(false)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun updateUIWithUserData(document: DocumentSnapshot) {
        val fullName = document.getString("fullName")
        val profilePicUrl = document.getString("profilePicUrl")

        binding.apply {
            tvFullName.text = fullName
            etFullName.setText(fullName)

            if (!profilePicUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(profilePicUrl)
                    .placeholder(R.drawable.default_pfp)
                    .error(R.drawable.default_pfp)
                    .into(ivProfilePicture)
            } else {
                ivProfilePicture.setImageResource(R.drawable.default_pfp)
            }
        }
    }

    private fun enterEditMode() {
        binding.apply {
            tvFullName.visibility = View.GONE
            btnEdit.visibility = View.GONE
            tilFullName.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            btnChangePicture.visibility = View.VISIBLE
        }
    }

    private fun saveEditChanges(userId: String) {
        showLoading(true)

        if (shouldUpdateProfilePicture) {
            uploadNewProfilePicture(userId)
        } else {
            updateProfile(userId, binding.etFullName.text.toString().trim(), null)
        }

        exitEditMode()
    }

    private fun exitEditMode() {
        binding.apply {
            tvFullName.visibility = View.VISIBLE
            btnEdit.visibility = View.VISIBLE
            tilFullName.visibility = View.GONE
            btnSave.visibility = View.GONE
            btnChangePicture.visibility = View.GONE
        }
    }

    private fun uploadNewProfilePicture(userId: String) {
        val bitmap = (binding.ivProfilePicture.drawable as? BitmapDrawable)?.bitmap ?: return

        model.uploadImageToCloudinary(
            bitmap,
            "${userId}_profile_pic",
            { imageUrl -> updateProfile(userId, binding.etFullName.text.toString().trim(), imageUrl) },
            { error ->
                Log.e("Cloudinary", "Upload error: ${error ?: "Unknown error"}")
                showToast("Failed to upload new profile picture")
                showLoading(false)
            }
        )
    }

    private fun updateProfile(userId: String, newFullName: String, newImageUrl: String?) {
        val userUpdates = mutableMapOf<String, Any>("fullName" to newFullName)
        newImageUrl?.let { userUpdates["profilePicUrl"] = it }

        firestore.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                showToast("Profile updated")
                shouldUpdateProfilePicture = false
                loadUserProfile()
            }
            .addOnFailureListener {
                showToast("Failed to update profile")
                showLoading(false)
            }
    }

    private fun pickImageFromGallery() {
        cameraLauncher.launch(null)
    }

    private fun signOut() {
        firebaseAuth.signOut()
        navigateToAuth()
    }

    private fun navigateToAuth() {
        requireActivity().finish()
        startActivity(Intent(requireContext(), AuthActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}