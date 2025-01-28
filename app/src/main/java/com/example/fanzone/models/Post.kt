package com.example.fanzone.models

import java.util.*

data class Post(
    val id: String,
    val username: String,
    val profileImageUrl: String?,
    val timePosted: Date,
    val content: String,
    var likeCount: Int,
    val matchId: String
)
