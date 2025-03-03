import org.jetbrains.kotlin.konan.properties.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.6.0" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

val localProperties = rootProject.file("local.properties").let { file ->
    Properties().apply { load(file.inputStream()) }
}

val cloudinaryApiKey: String = localProperties.getProperty("cloudinary_api_key") ?: ""
val cloudinaryApiSecret: String = localProperties.getProperty("cloudinary_api_secret") ?: ""

rootProject.extra["cloudinaryApiKey"] = cloudinaryApiKey
rootProject.extra["cloudinaryApiSecret"] = cloudinaryApiSecret