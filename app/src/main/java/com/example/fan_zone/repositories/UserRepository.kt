package com.example.fan_zone.repositories

import com.example.fan_zone.models.FirebaseModel
import com.example.fan_zone.models.User

class UserRepository {
    private val firebaseModel = FirebaseModel.shared

    fun getCurrentUserId(): String? {
        return firebaseModel.getCurrentUserId()
    }

    fun signOut() {
        firebaseModel.signOutUser()
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        firebaseModel.getUserById(userId, callback)
    }

    fun updateUserProfile(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        firebaseModel.updateUserProfile(userId, updates, onSuccess, onFailure)
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseModel.loginUser(email, password, onSuccess, onFailure)
    }

    fun createUser(
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseModel.createUser(fullName, email, password, onSuccess, onFailure)
    }
}
