package com.example.fan_zone.repositories

import com.example.fan_zone.models.FirebaseModel
import com.example.fan_zone.models.Post

class PostRepository {
    private val firebaseModel = FirebaseModel.shared

    suspend fun createPost(post: Post): Post {
        return firebaseModel.createPost(post)
    }

    suspend fun deletePost(postId: String) {
        firebaseModel.deletePost(postId)
    }

    suspend fun updatePost(
        postId: String,
        content: String,
        imageUrl: String?,
        likedUserIds: List<String>? = null
    ) {
        firebaseModel.updatePost(postId, content, imageUrl, likedUserIds)
    }

    suspend fun getPostsByMatchID(matchId: Int): List<Post> {
        return firebaseModel.getPostsByMatchId(matchId)
    }

    suspend fun getPostsByUserId(userId: String): List<Post> {
        return firebaseModel.getPostsByUserId(userId)
    }

    suspend fun updatePostLikes(postId: String, likedUserIds: List<String>) {
        firebaseModel.updatePostLikes(postId, likedUserIds)
    }

    suspend fun getAllPosts(): List<Post> {
        return firebaseModel.getAllPosts()
    }
}
