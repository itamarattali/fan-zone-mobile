package com.example.fanzone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fanzone.R
import com.example.fanzone.models.Post
import com.google.android.material.textview.MaterialTextView

class PostAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: MaterialTextView = view.findViewById(R.id.userNameTextView)
        val timestamp: MaterialTextView = view.findViewById(R.id.postTimestampTextView)
        val content: MaterialTextView = view.findViewById(R.id.postContentTextView)
        val likes: MaterialTextView = view.findViewById(R.id.postLikesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.userName.text = post.userName
        holder.timestamp.text = post.timestamp
        holder.content.text = post.content
        holder.likes.text = "Likes: ${post.likes}"
    }

    override fun getItemCount(): Int = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
