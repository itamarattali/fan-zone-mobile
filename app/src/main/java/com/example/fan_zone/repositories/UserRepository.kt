package com.example.fan_zone.repositories

import com.example.fan_zone.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUserData(userId: String): User? {
        return try {
            val document = usersCollection
                .document(userId)
                .get()
                .await()  // Wait for the result asynchronously

            if (document.exists()) {
                document.toObject(User::class.java) // Return the user object
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
