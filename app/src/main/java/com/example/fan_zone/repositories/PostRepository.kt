package com.example.fan_zone.repositories

import com.example.fan_zone.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import android.util.Log

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val postCollection = db.collection("posts")

    suspend fun createPost(post: Post): Post {
        try {
            val postId: String = postCollection.document().id
            val newPost: Post = post.copy(id = postId)
            postCollection.document(postId).set(newPost).await()
            return newPost
        } catch (e: Exception) {
            throw Exception("Error creating post: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            postCollection.document(postId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error deleting post: ${e.message}")
        }
    }

    suspend fun updatePost(postId: String, content: String, imageUrl: String?, likedUserIds: List<String>? = null) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "content" to content,
                "imageUrl" to imageUrl
            )
            
            if (likedUserIds != null) {
                updates["likedUserIds"] = likedUserIds
            }

            postCollection.document(postId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error updating post: ${e.message}")
        }
    }

    suspend fun getPostsByMatchID(matchId: Int): List<Post> {
        return try {
            val snapshot = postCollection
                .whereEqualTo("matchId", matchId.toString())
                .orderBy("timePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPostsByUserId(userId: String): List<Post> {
        return try {
            Log.d("PostRepository", "Fetching posts for user: $userId")
            val snapshot = postCollection
                .whereEqualTo("userId", userId)
                .orderBy("timePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            Log.d("PostRepository", "Fetched ${snapshot.documents.size} documents")
            val posts = snapshot.toObjects(Post::class.java)
            Log.d("PostRepository", "Converted to ${posts.size} posts")
            posts
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching posts: ${e.message}")
            emptyList()
        }
    }
}
