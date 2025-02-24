package com.example.fan_zone.repositories

import com.example.fan_zone.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val postCollection = db.collection("posts")

    suspend fun createPost(post: Post) {
        try {
            val postId: String = postCollection.document().id
            val newPost: Post = post.copy(id = postId)
            postCollection.document(postId).set(newPost).await()
        } catch (e: Exception) {
            throw Exception("Error creating post: ${e.message}")
        }
    }

    suspend fun getPostsByMatchID(matchId: Int): List<Post> {
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
