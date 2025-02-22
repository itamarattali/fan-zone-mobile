package com.example.fanzone.apiCalls

import android.util.Log
import com.example.fanzone.model.ListMatch
import com.example.fanzone.model.parseMatches
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FootballData {
    fun getMatches(callback: (MutableList<ListMatch>) -> Unit) {
        val apiService = RetrofitClient.instance
        apiService.getMatches("2025-02-01", "2025-03-01")
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val jsonObject = JSONObject(response.body()!!) // Convert raw JSON string to JSONObject
                            val jsonMatches: JSONArray = jsonObject.getJSONArray("matches")
                            val matches = parseMatches(jsonMatches)
                            Log.d("FootballData", "Matches: $matches")
                            callback(matches)
                        } catch (e: Exception) {
                            Log.e("FootballData", "JSON parsing error: ${e.message}")
                        }
                    } else {
                        Log.e("FootballData", "API call failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("FootballData", "Network error: ${t.message}")
                }
            })
    }
}
