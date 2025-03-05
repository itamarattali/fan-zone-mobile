package com.example.fanzone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fanzone.models.MatchDetails
import com.example.fanzone.models.Post
import com.example.fanzone.repositories.MatchDetailsRepository
import kotlinx.coroutines.launch
import java.util.*

class MatchDetailsViewModel : ViewModel() {

    private val repository = MatchDetailsRepository()

    private val _matchDetails = MutableLiveData<MatchDetails>()
    val matchDetails: LiveData<MatchDetails> get() = _matchDetails

    private val _popularPosts = MutableLiveData<List<Post>>()
    val popularPosts: LiveData<List<Post>> get() = _popularPosts

    private val _yourPosts = MutableLiveData<List<Post>>()
    val yourPosts: LiveData<List<Post>> get() = _yourPosts

    fun initialize(matchId: String) {
        fetchMatchDetails(matchId)
        fetchYourPosts(matchId)
        fetchPopularPosts(matchId)
    }

    private fun fetchYourPosts(matchId: String) {
        viewModelScope.launch {
            _yourPosts.value = repository.fetchYourPosts(matchId)
        }
    }

    private fun fetchPopularPosts(matchId: String) {
        viewModelScope.launch {
            _popularPosts.value = repository.fetchPopularPosts(matchId)
        }
    }

    private fun fetchMatchDetails(matchId: String) {
        viewModelScope.launch {
            _matchDetails.value = repository.fetchMatchDetails(matchId)
        }
    }

    fun addPost(content: String, matchId: String, user: String) {
        val post = Post(
            postId = UUID.randomUUID().toString(),
            userName = user,
            timestamp = System.currentTimeMillis().toString(),
            likes = 0,
            matchId = matchId,
            content = content
        )
        viewModelScope.launch {
            repository.addPost(post)
            fetchYourPosts(matchId) // Refresh posts after adding
        }
    }
}
