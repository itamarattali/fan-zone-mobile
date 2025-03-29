package com.example.fan_zone.apiCalls

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {
    @GET("competitions/PL/matches")
    fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Call<String>
}
