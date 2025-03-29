package com.example.fan_zone.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseModel private constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        val shared = FirebaseModel()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.toObject(User::class.java))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun signOutUser() {
        auth.signOut()
    }

    fun createUser(
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = mapOf(
                            "fullName" to fullName,
                            "email" to email,
                            "profilePicUrl" to ""
                        )
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure("Failed to save user data") }
                    }
                } else {
                    onFailure("Registration failed: ${task.exception?.message}")
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

    suspend fun createPost(post: Post): Post {
        return try {
            val postId = db.collection("posts").document().id
            val newPost = post.copy(id = postId)
            db.collection("posts").document(postId).set(newPost).await()
            newPost
        } catch (e: Exception) {
            throw Exception("Error creating post: ${e.message}")
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            db.collection("posts").document(postId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error deleting post: ${e.message}")
        }
    }

    suspend fun updatePost(
        postId: String,
        content: String,
        imageUrl: String?,
        likedUserIds: List<String>? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "content" to content,
                "imageUrl" to imageUrl
            )

            if (likedUserIds != null) {
                updates["likedUserIds"] = likedUserIds
            }

            db.collection("posts").document(postId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error updating post: ${e.message}")
        }
    }

    suspend fun getPostsByMatchId(matchId: Int): List<Post> {
        return try {
            val snapshot = db.collection("posts")
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
            val snapshot = db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updatePostLikes(postId: String, likedUserIds: List<String>) {
        try {
            db.collection("posts").document(postId)
                .update("likedUserIds", likedUserIds)
                .await()
        } catch (e: Exception) {
            throw Exception("Error updating post likes: ${e.message}")
        }
    }

    suspend fun getAllPosts(): List<Post> {
        return try {
            val snapshot = db.collection("posts")
                .orderBy("timePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
