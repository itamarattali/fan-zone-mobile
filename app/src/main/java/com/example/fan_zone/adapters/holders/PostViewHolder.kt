package com.example.fan_zone.adapters.holders

import android.annotation.SuppressLint
import android.graphics.ImageDecoder
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.R
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
import com.example.fan_zone.models.CloudinaryModel
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.UserRepository
import com.squareup.picasso.Picasso

class PostViewHolder(private val binding: PostRecyclerViewItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var currentImageUrl: String? = null
    private var isImageRemoved = false
    private var pendingImageUri: Uri? = null

    private val cancelEditText = "Cancel edit"

    @SuppressLint("SetTextI18n")
    fun bind(
        post: Post,
        onLikeClicked: (Post) -> Unit,
        onUnlikeClicked: (Post) -> Unit,
        onEditPost: (String, String, String?) -> Unit,
        onDeletePost: (Post) -> Unit,
        onImageEditRequest: (Post) -> Unit,
        onLoadingStateChanged: (Boolean) -> Unit,
        userRepository: UserRepository
    ) {
        binding.contentTextView.text = post.content
        binding.likeCountTextView.text = "${post.likedUserIds.size} likes"

        post.imageUrl?.let { imageUrl ->
            currentImageUrl = imageUrl
            binding.postImageView.visibility = View.VISIBLE
            Picasso.get()
                .load(imageUrl)
                .rotate(0f)
                .fit()
                .centerInside()
                .into(binding.postImageView)
        } ?: run {
            binding.postImageView.visibility = View.GONE
            currentImageUrl = null
        }

        val currentUserId = userRepository.getCurrentUserId()

        userRepository.getUserById(post.userId) { user ->
            binding.usernameTextView.text = if (user != null) user.fullName else "Unknown User"

            val profileImageUrl = if (user != null && !user.profilePicUrl.isNullOrEmpty()) {
                user.profilePicUrl
            } else {
                null
            }

            Picasso.get()
                .load(profileImageUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.profileImageView)
        }

        if (post.userId == currentUserId) {
            binding.editPostText.visibility = View.VISIBLE
            binding.deletePostText.visibility = View.VISIBLE
        } else {
            binding.editPostText.visibility = View.GONE
            binding.deletePostText.visibility = View.GONE
        }

        fun toggleEditMode(isEditing: Boolean) {
            binding.apply {
                contentTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
                likeIcon.visibility = if (isEditing) View.GONE else View.VISIBLE
                editPostContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
                editActionsContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
                submitEditButton.visibility = if (isEditing) View.VISIBLE else View.GONE
                imageEditControls.visibility = if (isEditing) View.VISIBLE else View.GONE
                changeImageButton.text =
                    if (currentImageUrl != null || pendingImageUri != null) "Change Image" else "Add Image"
                removeImageButton.visibility =
                    if (currentImageUrl != null || pendingImageUri != null) View.VISIBLE else View.GONE
                editPostText.text = if (isEditing) cancelEditText else "Edit post"

                if (!isEditing) {
                    pendingImageUri = null
                    isImageRemoved = false

                    if (currentImageUrl != null) {
                        postImageView.visibility = View.VISIBLE
                        Picasso.get()
                            .load(currentImageUrl)
                            .rotate(0f)
                            .fit()
                            .centerInside()
                            .into(postImageView)
                    } else {
                        postImageView.visibility = View.GONE
                    }
                } else {
                    pendingImageUri?.let { uri ->
                        postImageView.visibility = View.VISIBLE
                        Picasso.get().load(uri).into(postImageView)
                    }
                }
            }
        }

        binding.editPostText.setOnClickListener {
            if (binding.editPostText.text == cancelEditText) {
                toggleEditMode(false)
            } else {
                isImageRemoved = false
                pendingImageUri = null
                binding.editPostEditText.setText(post.content)
                toggleEditMode(true)
            }
        }

        binding.changeImageButton.setOnClickListener {
            onImageEditRequest(post)
        }

        binding.removeImageButton.setOnClickListener {
            isImageRemoved = true
            binding.postImageView.visibility = View.GONE
            binding.imageEditControls.visibility = View.VISIBLE
            binding.removeImageButton.visibility = View.GONE
        }

        binding.submitEditButton.setOnClickListener {
            val updatedContent = binding.editPostEditText.text.toString().trim()
            if (updatedContent.isNotEmpty()) {
                if (pendingImageUri != null && !isImageRemoved) {
                    onLoadingStateChanged(true)
                    val source = ImageDecoder.createSource(
                        binding.root.context.contentResolver,
                        pendingImageUri!!
                    )
                    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }

                    CloudinaryModel.shared.uploadImage(
                        bitmap = bitmap,
                        name = "post_${System.currentTimeMillis()}",
                        onSuccess = { imageUrl ->
                            onEditPost(post.id, updatedContent, imageUrl)
                            binding.contentTextView.text = updatedContent
                            currentImageUrl = imageUrl

                            binding.postImageView.visibility = View.VISIBLE
                            Picasso.get()
                                .load(imageUrl)
                                .rotate(0f)
                                .noFade()
                                .into(binding.postImageView)

                            toggleEditMode(false)
                            isImageRemoved = false
                            pendingImageUri = null
                            onLoadingStateChanged(false)
                        },
                        onError = { error ->
                            onLoadingStateChanged(false)
                            Toast.makeText(
                                binding.root.context,
                                "Failed to upload image: $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    val finalImageUrl = if (isImageRemoved) null else currentImageUrl
                    onEditPost(post.id, updatedContent, finalImageUrl)
                    binding.contentTextView.text = updatedContent
                    if (isImageRemoved) {
                        currentImageUrl = null
                    }
                    toggleEditMode(false)
                    isImageRemoved = false
                    pendingImageUri = null
                }
            }
        }

        binding.deletePostText.setOnClickListener {
            onDeletePost(post)
        }

        currentUserId?.let { userId ->
            updateLikeUI(post, userId)
            binding.likeIcon.setOnClickListener {
                if (post.likedUserIds.contains(userId)) {
                    onUnlikeClicked(post)
                } else {
                    onLikeClicked(post)
                }
            }
        }
    }

    fun onImagePreview(uri: Uri) {
        pendingImageUri = uri
        isImageRemoved = false
        binding.postImageView.visibility = View.VISIBLE

        val source = ImageDecoder.createSource(binding.root.context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, source ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
        binding.postImageView.setImageBitmap(bitmap)

        binding.removeImageButton.visibility = View.VISIBLE
        binding.changeImageButton.text = "Change Image"
    }

    @SuppressLint("SetTextI18n")
    private fun updateLikeUI(post: Post, userId: String) {
        val isLiked = post.likedUserIds.contains(userId)
        binding.likeIcon.setImageResource(
            if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_unfilled
        )
        binding.likeCountTextView.text = "${post.likedUserIds.size} likes"
    }

    fun cancelEditMode() {
        binding.apply {
            contentTextView.visibility = View.VISIBLE
            editPostContainer.visibility = View.GONE
            editActionsContainer.visibility = View.GONE
            submitEditButton.visibility = View.GONE
            imageEditControls.visibility = View.GONE
            editPostText.text = "Edit post"

            currentImageUrl?.let {
                postImageView.visibility = View.VISIBLE
                Picasso.get()
                    .load(it)
                    .rotate(0f)
                    .fit()
                    .centerInside()
                    .into(postImageView)
            } ?: run {
                postImageView.visibility = View.GONE
            }
        }
    }
}
