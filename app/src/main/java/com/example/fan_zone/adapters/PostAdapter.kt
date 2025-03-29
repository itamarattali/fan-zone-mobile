package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.graphics.ImageDecoder
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.R
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
import com.example.fan_zone.models.Model
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onUnlikeClicked: (Post) -> Unit,
    private val onEditPost: (String, String, String?) -> Unit,
    private val onDeletePost: (Post) -> Unit,
    private val onImageEditRequest: (Post) -> Unit,
    private val onLoadingStateChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()
    private val userRepository = UserRepository()
    private var recyclerView: RecyclerView? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            PostRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(
            posts[position],
            onLikeClicked,
            onUnlikeClicked,
            onEditPost,
            onDeletePost,
            onImageEditRequest,
            onLoadingStateChanged,
            userRepository
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty() && payloads[0] is Uri) {
            holder.onImagePreview(payloads[0] as Uri)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = posts.size

    fun showImagePreview(postId: String, uri: Uri) {
        val position = posts.indexOfFirst { it.id == postId }
        if (position != -1) {
            notifyItemChanged(position, uri)
        }
    }

    fun cancelEdit(postId: String) {
        val position = posts.indexOfFirst { it.id == postId }
        if (position != -1) {
            val holder = recyclerView?.findViewHolderForAdapterPosition(position) as? PostViewHolder
            holder?.cancelEditMode()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    class PostViewHolder(private val binding: PostRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentImageUrl: String? = null
        private var isImageRemoved = false
        private var pendingImageUri: Uri? = null

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
                    .rotate(0f)  // Add rotation handling
                    .fit()
                    .centerInside()
                    .into(binding.postImageView)
            } ?: run {
                binding.postImageView.visibility = View.GONE
                currentImageUrl = null
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserData(post.userId)

                binding.usernameTextView.text = if (user != null) user.fullName else "Unknown User"

                val imageUrl = if (user != null && !user.profilePicUrl.isNullOrEmpty()) {
                    user.profilePicUrl
                } else {
                    null
                }

                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.profileImageView)
            }

            // Show Edit Post link only for post author
            if (post.userId == userId) {
                binding.editPostText.visibility = View.VISIBLE
                binding.deletePostText.visibility = View.VISIBLE
            } else {
                binding.editPostText.visibility = View.GONE
                binding.deletePostText.visibility = View.GONE
            }

            fun toggleEditMode(isEditing: Boolean) {
                binding.apply {
                    contentTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
                    editPostContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
                    editActionsContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
                    submitEditButton.visibility = if (isEditing) View.VISIBLE else View.GONE
                    imageEditControls.visibility = if (isEditing) View.VISIBLE else View.GONE
                    changeImageButton.text =
                        if (currentImageUrl != null || pendingImageUri != null) "Change Image" else "Add Image"
                    removeImageButton.visibility =
                        if (currentImageUrl != null || pendingImageUri != null) View.VISIBLE else View.GONE
                    editPostText.text = if (isEditing) "cancel edit" else "Edit post"

                    if (!isEditing) {
                        // Reset image state when canceling edit
                        pendingImageUri = null
                        isImageRemoved = false

                        // Restore original image if it exists
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
                        // Show preview if there's a pending image
                        pendingImageUri?.let { uri ->
                            postImageView.visibility = View.VISIBLE
                            Picasso.get().load(uri).into(postImageView)
                        }
                    }
                }
            }

            binding.editPostText.setOnClickListener {
                if (binding.editPostText.text == "cancel edit") {
                    // Cancel edit
                    toggleEditMode(false)
                } else {
                    // Start edit
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

                        Model.shared.uploadImageToCloudinary(
                            bitmap = bitmap,
                            name = "post_${System.currentTimeMillis()}",
                            onSuccess = { imageUrl ->
                                // Update the post first
                                onEditPost(post.id, updatedContent, imageUrl)
                                binding.contentTextView.text = updatedContent
                                currentImageUrl = imageUrl  // Update current image URL

                                // Then load the image with a callback
                                binding.postImageView.visibility = View.VISIBLE
                                Picasso.get()
                                    .load(imageUrl)
                                    .rotate(0f)
                                    .noFade()
                                    .into(binding.postImageView)

                                // Toggle edit mode off immediately after updating content
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
                        // No new image, just update content and possibly remove existing image
                        val finalImageUrl = if (isImageRemoved) null else currentImageUrl
                        onEditPost(post.id, updatedContent, finalImageUrl)
                        binding.contentTextView.text = updatedContent
                        if (isImageRemoved) {
                            currentImageUrl = null  // Update current image URL if removed
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

            updateLikeUI(post, userId)
            binding.likeIcon.setOnClickListener {
                if (post.likedUserIds.contains(userId)) {
                    onUnlikeClicked(post)
                } else {
                    onLikeClicked(post)
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
        private fun updateLikeUI(post: Post, userId: String?) {
            val isLiked = userId != null && post.likedUserIds.contains(userId)
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
                
                // Reset image to original state if needed
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
}