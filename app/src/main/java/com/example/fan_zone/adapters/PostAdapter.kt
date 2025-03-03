package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.R
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
import com.example.fan_zone.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onUnlikeClicked: (Post) -> Unit,
    private val onEditPost: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val posts = mutableListOf<Post>()
    private var currentUsername = ""

    init {
        fetchCurrentUsername()
    }

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
        holder.bind(posts[position], onLikeClicked, onUnlikeClicked, onEditPost, currentUsername)
    }

    override fun getItemCount() = posts.size

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchCurrentUsername() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                currentUsername = document.getString("username").orEmpty()
                notifyDataSetChanged()
            }
        }
    }

    class PostViewHolder(private val binding: PostRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post, onLikeClicked: (Post) -> Unit, onUnlikeClicked: (Post) -> Unit, onEditPost: (Post) -> Unit, userId: String?) {
            binding.usernameTextView.text = post.username
            binding.contentTextView.text = post.content
            binding.likeCountTextView.text = "${post.likeCount} likes"

            // Load profile image
            Picasso.get().load(post.profileImageUrl ?: "").into(binding.profileImageView)

            // Show Edit Post link only for post author
            if (post.username == userId) binding.editPostText.visibility = View.VISIBLE
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
                if (post.likedUsers.contains(userId)) {
                    onUnlikeClicked(post)
                } else {
                    onLikeClicked(post)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun updateLikeUI(post: Post, userId: String?) {
            // Check if user has liked the post
            val isLiked = userId != null && post.likedUsers.contains(userId)

            // Show correct like/unlike icon
            binding.likeIcon.setImageResource(
                if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_unfilled
            )

            // Update like count dynamically
            binding.likeCountTextView.text = "${post.likeCount} likes"
        }

        private fun toggleEditMode(isEditing: Boolean) {
            binding.contentTextView.visibility = if (isEditing) View.GONE else View.VISIBLE
            binding.editPostContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
            binding.editActionsContainer.visibility = if (isEditing) View.VISIBLE else View.GONE
            binding.editPostText.visibility = if (isEditing) View.GONE else View.VISIBLE
        }
    }
}
