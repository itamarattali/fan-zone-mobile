package com.example.fanzone.apiCalls

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface FootballApiService {
    @Headers("X-Auth-Token: 81219d8b375e4fb9bd2bdcdb5665db12")
    @GET("competitions/PL/matches")
    fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Call<String> // Changed to Call<String> for raw JSON
}
