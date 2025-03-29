package com.example.fan_zone.fragments

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.AuthActivity
import com.example.fan_zone.R
import com.example.fan_zone.adapters.PostAdapter
import com.example.fan_zone.databinding.FragmentProfileBinding
import com.example.fan_zone.models.Model
import com.example.fan_zone.models.Post
import com.example.fan_zone.models.User
import com.example.fan_zone.repositories.UserRepository
import com.example.fan_zone.viewModels.ProfileViewModel
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var shouldUpdateProfilePicture = false
    private lateinit var model: Model
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postsAdapter: PostAdapter
    private var currentEditingPost: Post? = null
    private var editImageUri: Uri? = null
    private val userRepository = UserRepository()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding.ivProfilePicture.setImageBitmap(it)
            shouldUpdateProfilePicture = true
        }
    }

    private val getContentForEdit = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentEditingPost?.let { post ->
                editImageUri = it
                postsAdapter.showImagePreview(post.id, uri)
            }
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
        setupRecyclerView()
        setupObservers()
        loadUserProfile()
    }

    private fun initialize() {
        model = Model.shared
    }

    private fun setupUI() {
        with(binding) {
            btnEdit.setOnClickListener { enterEditMode() }
            btnSave.setOnClickListener {
                userRepository.getCurrentUserId()?.let { userId ->
                    saveEditChanges(userId)
                }
            }
            btnChangePicture.setOnClickListener { pickImageFromGallery() }
            btnSignOut.setOnClickListener { signOut() }
        }
    }

    private fun setupRecyclerView() {
        postsAdapter = PostAdapter(
            onLikeClicked = { post -> viewModel.likePost(post) },
            onUnlikeClicked = { post -> viewModel.unlikePost(post) },
            onEditPost = { postId, content, imageUrl ->
                viewModel.updatePost(postId, content, imageUrl)
                currentEditingPost = null  // Reset editing state after successful edit
                editImageUri = null
            },
            onDeletePost = { post -> viewModel.deletePost(post) },
            onImageEditRequest = { post -> 
                // If there's already a post being edited, cancel its edit mode first
                currentEditingPost?.let { currentPost ->
                    if (currentPost.id != post.id) {
                        postsAdapter.cancelEdit(currentPost.id)
                    }
                }
                currentEditingPost = post
                getContentForEdit.launch("image/*")
            },
            onLoadingStateChanged = { isLoading ->
                viewModel.setLoading(isLoading)
            }
        )

        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postsAdapter.submitList(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let { showToast(it) }
        }
    }

    private fun loadUserProfile() {
        val userId = userRepository.getCurrentUserId() ?: run {
            navigateToAuth()
            return
        }
        
        showLoading(true)
        viewModel.fetchUserPosts(userId)

        userRepository.getUserById(userId) { user ->
            showLoading(false)
            if (user != null) {
                updateUIWithUserData(user)
            } else {
                showToast("User data not found")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            tvFullName.text = user.fullName
            etFullName.setText(user.fullName)

            if (!user.profilePicUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user.profilePicUrl)
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
            btnSignOut.visibility = View.GONE
            tilFullName.visibility = View.VISIBLE
            editModeButtons.visibility = View.VISIBLE
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
            btnSignOut.visibility = View.VISIBLE
            tilFullName.visibility = View.GONE
            editModeButtons.visibility = View.GONE
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

        userRepository.updateUserProfile(
            userId,
            userUpdates,
            onSuccess = {
                showToast("Profile updated")
                shouldUpdateProfilePicture = false
                loadUserProfile()
            },
            onFailure = {
                showToast("Failed to update profile")
                showLoading(false)
            }
        )
    }

    private fun pickImageFromGallery() {
        cameraLauncher.launch(null)
    }

    private fun signOut() {
        userRepository.signOut()
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