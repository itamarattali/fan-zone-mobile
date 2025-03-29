package com.example.fan_zone.apiCalls

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import com.example.fan_zone.BuildConfig

const val apiKey = BuildConfig.MATCHES_API_KEY

interface FootballApiService {
    @Headers("X-Auth-Token: $apiKey")
    @GET("competitions/PL/matches")
    fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Call<String> // Changed to Call<String> for raw JSON
}