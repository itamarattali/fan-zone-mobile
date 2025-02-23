package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.R
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
import com.example.fan_zone.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onUnlikeClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Post>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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
        holder.bind(posts[position], onLikeClicked, onUnlikeClicked, currentUserId)
    }

    override fun getItemCount() = posts.size

    class PostViewHolder(private val binding: PostRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(post: Post, onLikeClicked: (Post) -> Unit, onUnlikeClicked: (Post) -> Unit, userId: String?) {
            binding.usernameTextView.text = post.username
            binding.contentTextView.text = post.content
            binding.likeCountTextView.text = "${post.likeCount} likes"

            // Load profile image
            Picasso.get().load(post.profileImageUrl ?: "").into(binding.profileImageView)

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
    }
}
