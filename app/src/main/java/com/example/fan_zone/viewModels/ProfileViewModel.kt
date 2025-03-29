package com.example.fan_zone.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.models.FirebaseModel
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.PostRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val postRepository = PostRepository()
    private val firebaseModel = FirebaseModel.shared
    
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchUserPosts(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val posts = postRepository.getPostsByUserId(userId)
                _userPosts.value = posts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load posts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                val currentUserId = firebaseModel.getCurrentUserId() ?: return@launch
                val updatedLikes = post.likedUserIds.toMutableList()
                if (!updatedLikes.contains(currentUserId)) {
                    updatedLikes.add(currentUserId)
                    postRepository.updatePost(post.id, post.content, post.imageUrl, updatedLikes)
                    fetchUserPosts(currentUserId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to like post: ${e.message}"
            }
        }
    }

    fun unlikePost(post: Post) {
        viewModelScope.launch {
            try {
                val currentUserId = firebaseModel.getCurrentUserId() ?: return@launch
                val updatedLikes = post.likedUserIds.toMutableList()
                if (updatedLikes.contains(currentUserId)) {
                    updatedLikes.remove(currentUserId)
                    postRepository.updatePost(post.id, post.content, post.imageUrl, updatedLikes)
                    fetchUserPosts(currentUserId)
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
                val currentUserId = firebaseModel.getCurrentUserId() ?: return@launch
                fetchUserPosts(currentUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update post: ${e.message}"
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post.id)
                val currentUserId = firebaseModel.getCurrentUserId() ?: return@launch
                fetchUserPosts(currentUserId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete post: ${e.message}"
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
} 