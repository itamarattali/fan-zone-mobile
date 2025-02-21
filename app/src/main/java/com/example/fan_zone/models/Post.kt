package com.example.fan_zone.models

import java.util.Date

data class Post(
    val id: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val timePosted: Date = Date(),
    val content: String = "",
    var likeCount: Int = 0,
    val matchId: String = "",
    val likedUsers: List<String> = emptyList()
)
