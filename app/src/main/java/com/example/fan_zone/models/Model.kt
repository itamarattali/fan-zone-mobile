package com.example.fan_zone.models

import android.graphics.Bitmap

class Model {
    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    fun uploadImageToCloudinary(
        bitmap: Bitmap,
        name: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit,
    ) {
        cloudinaryModel.uploadImage(
            bitmap = bitmap,
            name = name,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}