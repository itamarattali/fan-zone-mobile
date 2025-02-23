package com.example.fanzone.repositories

import com.example.fanzone.models.MatchDetails
import com.example.fanzone.models.Post
import kotlinx.coroutines.delay
import java.util.*

class MatchDetailsRepository {

    private val mockPosts = mutableListOf(
        Post("1", "Emma Johnson", null, Date(), "Loved the game!", 15, "1"),
        Post("2", "John Doe", null, Date(), "Great match!", 5, "1"),
        Post("3", "Alice Smith", null, Date(), "Amazing game!", 22, "2"),
        Post("4", "Mark Lee", null, Date(), "Could have been better!", 8, "2")
    )

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

    suspend fun fetchPosts(matchId: String): List<Post> {
        delay(1000) // Simulate network delay
        return mockPosts.filter { it.matchId == matchId }
    }

    suspend fun addPost(post: Post) {
        delay(500) // Simulate network delay
        mockPosts.add(0, post)
    }

    suspend fun likePost(postId: String) {
        delay(200) // Simulate network delay
        val post = mockPosts.find { it.id == postId }
        post?.let { it.likeCount += 1 }
    }
}
