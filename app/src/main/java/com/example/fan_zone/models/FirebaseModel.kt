package com.example.fan_zone.models

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseModel private constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        val shared = FirebaseModel()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
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
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener {
                                onFailure("Failed to save user data")
                            }
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
}
