package com.example.fan_zone.repositories

import com.example.fan_zone.models.FirebaseModel
import com.example.fan_zone.models.User

class UserRepository {
    private val firebaseModel = FirebaseModel.shared

    suspend fun getUserById(userId: String): User? {
        return firebaseModel.getUserById(userId)
    }
}
