import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.example.fan_zone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fan_zone"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["googleApiKey"] = localProperties["google_api_key"] as String
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields.put(
            "CLOUDINARY_API_KEY",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["cloudinary_api_key"]}\"",
                "Cloudinary API Key"
            )
        )
        variant.buildConfigFields.put(
            "CLOUDINARY_API_SECRET",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["cloudinary_api_secret"]}\"",
                "Cloudinary API Secret"
            )
        )
        variant.buildConfigFields.put(
            "CLOUDINARY_CLOUD_NAME",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["cloudinary_cloud_name"]}\"",
                "Cloudinary Cloud Name"
            )
        )
        variant.buildConfigFields.put(
            "GOOGLE_API_KEY",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["google_api_key"]}\"",
                "Google Api Key"
            )
        )
        variant.buildConfigFields.put(
            "MATCHES_API_KEY",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["matches_api_key"]}\"",
                "Matches API Key"
            )
        )
    }
}

dependencies {
    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase Auth
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Picasso
    implementation(libs.picasso)
    implementation(libs.cloudinary.cloudinary.android)

    // Map
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Matches Api
    implementation(libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    // Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    // Location Provider
    implementation(libs.play.services.location)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}