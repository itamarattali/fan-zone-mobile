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
    private val onImageEditRequest: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()
    private val userRepository = UserRepository()

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
            userRepository: UserRepository
        ) {
            binding.contentTextView.text = post.content
            binding.likeCountTextView.text = "${post.likedUserIds.size} likes"

            // Handle post image
            post.imageUrl?.let { imageUrl ->
                currentImageUrl = imageUrl
                binding.postImageView.visibility = View.VISIBLE
                Picasso.get()
                    .load(imageUrl)
                    .into(binding.postImageView)
            } ?: run {
                binding.postImageView.visibility = View.GONE
                currentImageUrl = null
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            // Fetch user data asynchronously using a coroutine
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserData(post.userId)

                if (user != null) {
                    binding.usernameTextView.text = user.fullName
                    if (user.profilePicUrl != "") {
                        Picasso.get()
                            .load(user.profilePicUrl)
                            .into(binding.profileImageView)
                    }
                } else {
                    binding.usernameTextView.text = "Unknown User"
                    Picasso.get().load("default_image_url").into(binding.profileImageView)
                }
            }

            // Show Edit/Delete options only for post author
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

                    // Show preview if there's a pending image
                    pendingImageUri?.let { uri ->
                        postImageView.visibility = View.VISIBLE
                        Picasso.get().load(uri).into(postImageView)
                    }
                }
            }

            // Handle edit mode
            binding.editPostText.setOnClickListener {
                isImageRemoved = false
                pendingImageUri = null
                binding.editPostEditText.setText(post.content)
                toggleEditMode(true)
            }

            binding.changeImageButton.setOnClickListener {
                onImageEditRequest(post)
            }

            binding.removeImageButton.setOnClickListener {
                isImageRemoved = true
                binding.postImageView.visibility = View.GONE
                binding.imageEditControls.visibility = View.VISIBLE
                binding.removeImageButton.visibility = View.GONE
                currentImageUrl = null
            }

            binding.submitEditButton.setOnClickListener {
                val updatedContent = binding.editPostEditText.text.toString().trim()
                if (updatedContent.isNotEmpty()) {
                    if (pendingImageUri != null && !isImageRemoved) {
                        // Upload new image
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
                                onEditPost(post.id, updatedContent, imageUrl)
                                binding.contentTextView.text = updatedContent
                                toggleEditMode(false)
                                isImageRemoved = false
                                pendingImageUri = null
                            },
                            onError = { error ->
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
                        toggleEditMode(false)
                        isImageRemoved = false
                        pendingImageUri = null
                    }
                }
            }

            binding.deletePostText.setOnClickListener {
                onDeletePost(post)
            }

            // Handle like/unlike
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
            Picasso.get().load(uri).into(binding.postImageView)
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
    }
}