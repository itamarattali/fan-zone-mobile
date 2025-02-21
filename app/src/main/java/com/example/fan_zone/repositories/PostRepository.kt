package com.example.fan_zone.repositories

import com.example.fan_zone.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val postCollection = db.collection("posts")

    suspend fun getPostsByMatchID(matchId: String): List<Post> {
        return try {
            val snapshot = postCollection
                .whereEqualTo("matchId", matchId)
                .orderBy("timePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
