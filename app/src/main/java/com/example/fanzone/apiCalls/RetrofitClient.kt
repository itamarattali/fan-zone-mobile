package com.example.fanzone.apiCalls

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.football-data.org/v4/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Logs full request & response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Attach interceptor
        .build()

    val instance: FootballApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Attach client with logging
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }
}
