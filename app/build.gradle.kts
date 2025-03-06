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
                "API Key"
            )
        )
        variant.buildConfigFields.put(
            "CLOUDINARY_API_SECRET",
            com.android.build.api.variant.BuildConfigField(
                "String",
                "\"${localProperties["cloudinary_api_secret"]}\"",
                "API Secret"
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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")


    // Location Provider
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}