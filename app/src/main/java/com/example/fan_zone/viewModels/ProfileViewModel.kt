package com.example.fan_zone.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.models.Post
import com.example.fan_zone.repositories.PostRepository
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

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
} 