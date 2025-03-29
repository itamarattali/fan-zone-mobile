package com.example.fan_zone.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val postRepository = PostRepository()
    
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Starting to fetch posts for user: $userId")
                val posts = postRepository.getPostsByUserId(userId)
                Log.d("ProfileViewModel", "Fetched ${posts.size} posts")
                _userPosts.value = posts
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching posts: ${e.message}")
                _errorMessage.value = "Failed to load posts: ${e.message}"
            }
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val updatedLikes = post.likedUserIds.toMutableList()
                if (!updatedLikes.contains(currentUserId)) {
                    updatedLikes.add(currentUserId)
                    postRepository.updatePost(post.id, post.content, post.imageUrl, updatedLikes)
                    fetchUserPosts(currentUserId) // Refresh posts
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like post: ${e.message}"
            }
        }
    }

    fun unlikePost(post: Post) {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val updatedLikes = post.likedUserIds.toMutableList()
                if (updatedLikes.contains(currentUserId)) {
                    updatedLikes.remove(currentUserId)
                    postRepository.updatePost(post.id, post.content, post.imageUrl, updatedLikes)
                    fetchUserPosts(currentUserId) // Refresh posts
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unlike post: ${e.message}"
            }
        }
    }

    fun updatePost(postId: String, content: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                postRepository.updatePost(postId, content, imageUrl)
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                fetchUserPosts(currentUserId) // Refresh posts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update post: ${e.message}"
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.id)
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                fetchUserPosts(currentUserId) // Refresh posts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete post: ${e.message}"
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
} 