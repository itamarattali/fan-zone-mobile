package com.example.fanzone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MatchDetailsViewModel : ViewModel() {

    // Dynamic variables
    private val _matchTitle = MutableLiveData<String>("Chelsea FC - Arsenal FC")
    val matchTitle: LiveData<String> get() = _matchTitle

    private val _matchDetails = MutableLiveData<String>("Premier League\n12.14.2024 - 17:00")
    val matchDetails: LiveData<String> get() = _matchDetails

    private val _yourPosts = MutableLiveData<List<String>>()
    val yourPosts: LiveData<List<String>> get() = _yourPosts

    private val _popularPosts = MutableLiveData<List<String>>()
    val popularPosts: LiveData<List<String>> get() = _popularPosts

    init {
        // Example data
        _yourPosts.value = listOf("Loved the characters and the plot twists!", "COYB!!!")
        _popularPosts.value = listOf(
            "Amazing game from both teams!",
            "Enjoyable game! Really liked how Palmer performed!"
        )
    }

    fun addPost(post: String) {
        val updatedPosts = _yourPosts.value?.toMutableList() ?: mutableListOf()
        updatedPosts.add(0, post)
        _yourPosts.value = updatedPosts
    }
}
