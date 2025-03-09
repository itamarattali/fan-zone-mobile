package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.R
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
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
    private val onEditPost: (Post) -> Unit
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
        val binding = PostRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], onLikeClicked, onUnlikeClicked, onEditPost, userRepository)
    }

    override fun getItemCount() = posts.size

    class PostViewHolder(private val binding: PostRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(
            post: Post,
            onLikeClicked: (Post) -> Unit,
            onUnlikeClicked: (Post) -> Unit,
            onEditPost: (Post) -> Unit,
            userRepository: UserRepository
        ) {
            binding.contentTextView.text = post.content
            binding.likeCountTextView.text = "${post.likedUserIds.size} likes"

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            // Fetch user data asynchronously using a coroutine
            CoroutineScope(Dispatchers.Main).launch {
                val user = userRepository.getUserData(post.userId)

                if (user != null) {
                    // Update the UI with the fetched user data
                    binding.usernameTextView.text = user.fullName
//                    Picasso.get()
//                        .load(user.profilePicUrl)
//                        .placeholder(R.drawable.ic_user_placeholder) // shown while loading
//                        .error(R.drawable.ic_user_placeholder) // shown if URL fails to load
//                        .into(binding.profileImageView)

                } else {
                    // Handle case where user is not found
                    binding.usernameTextView.text = "Unknown User"
                    Picasso.get().load("default_image_url").into(binding.profileImageView)
                }
            }

            // Show Edit Post link only for post author
            if (post.userId == userId) binding.editPostText.visibility = View.VISIBLE
            else binding.editPostText.visibility = View.GONE

            // Enable editing mode
            binding.editPostText.setOnClickListener {
                binding.editPostEditText.setText(post.content)
                toggleEditMode(true)
            }

            // Handle edit submission
            binding.submitEditButton.setOnClickListener {
                val updatedContent = binding.editPostEditText.text.toString().trim()
                if (updatedContent.isNotEmpty()) {
                    val newPost = post.copy(content = updatedContent)
                    onEditPost(newPost)
                    binding.contentTextView.text = updatedContent
                    toggleEditMode(false)
                }
            }

            binding.cancelEditButton.setOnClickListener {
                toggleEditMode(false)
            }

            // Dynamically toggle like/unlike button based on likedUsers
            updateLikeUI(post, userId)

            // Handle like/unlike click
            binding.likeIcon.setOnClickListener {
                if (post.likedUserIds.contains(userId)) {
                    onUnlikeClicked(post)
                } else {
                    onLikeClicked(post)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun updateLikeUI(post: Post, userId: String?) {
            // Check if user has liked the post
            val isLiked = userId != null && post.likedUserIds.contains(userId)

            // Show correct like/unlike icon
            binding.likeIcon.setImageResource(
                if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_unfilled
            )

            // Update like count dynamically
            binding.likeCountTextView.text = "${post.likedUserIds.size} likes"
        }

        private fun toggleEditMode(isEditing: Boolean) {
            binding.contentTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
            binding.editPostContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
            binding.editActionsContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
            binding.editPostText.visibility = if (isEditing) View.GONE else View.VISIBLE
        }
    }
}
