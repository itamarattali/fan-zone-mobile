package com.example.fanzone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fanzone.models.Post
import com.example.fanzone.repositories.MatchDetailsRepository
import kotlinx.coroutines.launch

class MatchDetailsViewModel : ViewModel() {

    private val repository = MatchDetailsRepository()

    private val _popularPosts = MutableLiveData<List<Post>>()
    val popularPosts: LiveData<List<Post>> get() = _popularPosts

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts

    fun fetchPosts(matchId: String) {
        viewModelScope.launch {
            val posts = repository.fetchPosts(matchId)
            _popularPosts.value = posts
            _userPosts.value = posts.filter { it.username == "current_user" } // Mock user
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            repository.likePost(post.id)
            fetchPosts(post.matchId)
        }
    }
}
