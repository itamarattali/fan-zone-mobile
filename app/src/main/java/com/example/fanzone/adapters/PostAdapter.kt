package com.example.fanzone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.fanzone.R
import com.example.fanzone.models.Post
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val postContentTextView: TextView = itemView.findViewById(R.id.postContentTextView)
        val likeButton: MaterialButton = itemView.findViewById(R.id.likeButton)
        val likeCountTextView: TextView = itemView.findViewById(R.id.likesCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_recycler_view_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        // Set profile picture, username, time, post content, likes
        holder.profileImageView.setImageResource(R.drawable.ic_user_placeholder)
        holder.usernameTextView.text = post.username
        holder.timeTextView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(post.timePosted)
        holder.postContentTextView.text = post.content
        holder.likeCountTextView.text = post.likeCount.toString()

        // Like button click listener
        holder.likeButton.setOnClickListener {
            onLikeClicked(post)
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}
