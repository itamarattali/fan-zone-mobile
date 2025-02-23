package com.example.fan_zone.fragments

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fan_zone.R
import com.example.fan_zone.databinding.FragmentProfileBinding
import com.example.fan_zone.models.Model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var shouldUpdateProfilePicture = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var model: Model

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            binding.ivProfilePicture.setImageBitmap(bitmap)
            shouldUpdateProfilePicture = true
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        model = Model.shared

        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName")
                        val profilePicUrl = document.getString("profilePicUrl")

                        binding.tvFullName.text = fullName
                        binding.etFullName.setText(fullName)

                        if (!profilePicUrl.isNullOrEmpty()) {
                            Picasso.get()
                                .load(profilePicUrl)
                                .placeholder(R.drawable.default_pfp)
                                .into(binding.ivProfilePicture)
                        }
                    } else {
                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnEdit.setOnClickListener {
            enterEditMode()
        }

        binding.btnSave.setOnClickListener {
            saveEditChanges(userId)
        }

        binding.btnChangePicture.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnSignOut.setOnClickListener {
            firebaseAuth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment) {
                popUpTo(R.id.profileFragment) { inclusive = true }
            }
        }
    }

    private fun enterEditMode() {
        binding.tvFullName.visibility = View.GONE
        binding.btnEdit.visibility = View.GONE
        binding.tilFullName.visibility = View.VISIBLE
        binding.btnSave.visibility = View.VISIBLE
        binding.btnChangePicture.visibility = View.VISIBLE
    }

    private fun saveEditChanges(userId: String?) {
        binding.tvFullName.visibility = View.VISIBLE
        binding.btnEdit.visibility = View.VISIBLE
        binding.tilFullName.visibility = View.GONE
        binding.btnSave.visibility = View.GONE
        binding.btnChangePicture.visibility = View.GONE

        if (userId == null) {
            return
        }

        if (shouldUpdateProfilePicture) {
            val bitmap = (binding.ivProfilePicture.drawable as BitmapDrawable).bitmap
            model.uploadImageToCloudinary(bitmap, "${userId}_profile_pic", { imageUrl ->
                run {
                    updateProfile(userId, binding.etFullName.text.toString().trim(), imageUrl)
                }
            }, {
                updateProfile(userId, binding.etFullName.text.toString().trim(), "")
                Toast.makeText(context, "Failed to upload new profile picture", Toast.LENGTH_SHORT)
                    .show()
            })
        }
    }

    private fun updateProfile(userId: String, newFullName: String, newImageUrl: String?) {
        val userUpdates = mutableMapOf("fullName" to newFullName)
        newImageUrl?.let { userUpdates["profilePicUrl"] = it }

        firestore.collection("users").document(userId)
            .update(userUpdates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to update profile",
                    Toast.LENGTH_SHORT
                ).show()
            }


    }

    private fun pickImageFromGallery() {
        cameraLauncher.launch(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
