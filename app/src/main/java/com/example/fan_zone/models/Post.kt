package com.example.fan_zone.models

import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val timePosted: Date = Date(),
    val content: String = "",
    var likeCount: Int = 0,
    var location: GeoPoint? = null,
    val matchId: String = "",
    val likedUsersIds: List<String> = emptyList()
) {
    constructor() : this("", "", Date(), "", 0, null, "", emptyList())
}
