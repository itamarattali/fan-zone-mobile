package com.example.fan_zone.repositories

import com.example.fan_zone.models.Match
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fan_zone.apiCalls.RetrofitClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MatchRepository {
    private val _matches = MutableLiveData<MutableList<Match>>()
    val matches: LiveData<MutableList<Match>> get() = _matches

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        getMatches()
    }

    // TODO implement fromDate and toDate
    private fun getMatches() {
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
                            _matches.postValue(matches)
                            Log.d("ListMatchViewModel", "Matches updated: $matches")
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

    fun getMatchById(matchId: Int): Match? {
        return matches.value?.find{ match -> match.id == matchId }
    }

    fun getMatchesByDate(date: Date?): LiveData<MutableList<Match>> {
        val filteredMatches = MutableLiveData<MutableList<Match>>()
        if (date != null) {
            filteredMatches.value = _matches.value?.filter{dateFormat.format(it.date) == dateFormat.format(date)}?.toMutableList()
        }
        Log.d("matches", "getMatchesByDate: " + matches.value.toString())
        Log.d("filteredMatches", "getMatchesByDate: " + filteredMatches.value.toString())
        return filteredMatches
    }

    private fun parseMatches(jsonArray: JSONArray): MutableList<Match> {
        val matches = mutableListOf<Match>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val utcDate: Date = inputFormat.parse(jsonObject.getString("utcDate"))!!
            val homeTeam = jsonObject.getJSONObject("homeTeam").getString("shortName").toString()
            val awayTeam = jsonObject.getJSONObject("awayTeam").getString("shortName").toString()
//            val score = jsonObject.getJSONObject("score").getJSONObject("fullTime")
//            val homeTeamGoals = score.getInt("home")
//            val awayTeamGoals = score.getInt("away")

            matches.add(Match(id, utcDate, homeTeam, awayTeam, 0, 0))
        }

        return matches
    }
}
