package com.example.fanzone.repositories

import com.example.fanzone.models.MatchDetails
import com.example.fanzone.models.Post
import kotlinx.coroutines.delay

class MatchDetailsRepository {

    private val mockPosts = mutableListOf<Post>()
    private val mockMatchDetails = listOf(
        MatchDetails(
            matchId = "1",
            matchTime = "12.14.2024 - 17:00",
            matchLocation = "Stamford Bridge, London",
            homeTeam = "Chelsea FC",
            awayTeam = "Arsenal FC",
            result = "2-1"
        ),
        MatchDetails(
            matchId = "2",
            matchTime = "12.15.2024 - 18:00",
            matchLocation = "Old Trafford, Manchester",
            homeTeam = "Manchester United",
            awayTeam = "Liverpool",
            result = "1-1"
        )
    )

    suspend fun fetchMatchDetails(matchId: String): MatchDetails? {
        delay(1000) // Simulate network delay
        return mockMatchDetails.find { it.matchId == matchId }
    }

    suspend fun fetchYourPosts(matchId: String): List<Post> {
        delay(1000) // Simulate network delay
        // TODO when dor finished user authentication get the userId
        return mockPosts.filter { it.matchId == matchId }
    }

    suspend fun fetchPopularPosts(matchId: String): List<Post> {
        delay(1000) // Simulate network delay
        return mockPosts.filter { it.matchId == matchId }.sortedBy { it.likes.dec() }
    }

    suspend fun addPost(post: Post) {
        delay(500) // Simulate network delay
        mockPosts.add(0, post)
    }
}
