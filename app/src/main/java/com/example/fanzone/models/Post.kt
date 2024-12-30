package com.example.fanzone.models

data class Post(
    val postId: String,
    val matchId: String,
    val userName: String,
    val timestamp: String,
    val content: String,
    val likes: Int
)
