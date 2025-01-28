package com.example.fan_zone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.fan_zone.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
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
        storage = FirebaseStorage.getInstance()

        val userId = firebaseAuth.currentUser?.uid

        // Load User Data
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
    }

    private fun saveEditChanges(userId: String?) {
        binding.tvFullName.visibility = View.VISIBLE
        binding.btnEdit.visibility = View.VISIBLE
        binding.tilFullName.visibility = View.GONE
        binding.btnSave.visibility = View.GONE

        val newFullName = binding.etFullName.text.toString().trim()
        if (userId != null && newFullName.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .update("fullName", newFullName)
                .addOnSuccessListener {
                    binding.tvFullName.text = newFullName
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }

        // If a new profile picture was selected, upload it
        if (selectedImageUri != null && userId != null) {
            uploadProfilePicture(userId)
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun uploadProfilePicture(userId: String) {
        val profilePicRef = storage.reference.child("profile_pictures/$userId.jpg")
        profilePicRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // Get the download URL and save it to Firestore
                profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                    firestore.collection("users").document(userId)
                        .update("profilePicUrl", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT)
                                .show()
                            // Update UI
                            Picasso.get().load(uri).into(binding.ivProfilePicture)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to save profile picture",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload profile picture", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
