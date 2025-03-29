package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.adapters.holders.PostViewHolder
import com.example.fan_zone.databinding.PostRecyclerViewItemBinding
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.UserRepository

class PostAdapter(
    private val onLikeClicked: (Post) -> Unit,
    private val onUnlikeClicked: (Post) -> Unit,
    private val onEditPost: (String, String, String?) -> Unit,
    private val onDeletePost: (Post) -> Unit,
    private val onImageEditRequest: (Post) -> Unit,
    private val onLoadingStateChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<PostViewHolder>() {

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
}