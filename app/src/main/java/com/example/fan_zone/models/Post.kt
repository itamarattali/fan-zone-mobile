package com.example.fan_zone.models

import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val timePosted: Date = Date(),
    val content: String = "",
    var location: GeoPoint? = null,
    val matchId: String = "",
    val likedUserIds: List<String> = emptyList()
) {
    constructor() : this("", "", Date(), "", null, "", emptyList())
}
