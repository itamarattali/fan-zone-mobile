package com.example.fan_zone.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fan_zone.apiCalls.RetrofitClient
import com.example.fan_zone.database.MatchDatabase
import com.example.fan_zone.models.Match
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class MatchRepository(context: Context) {

    private val matchDao = MatchDatabase.getDatabase(context).matchDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchMatches() // Fetch matches from the API when the repository is initialized
    }

    // Fetch matches from the API and insert them into the Room database
    private fun fetchMatches() {
        val apiService = RetrofitClient.instance
        apiService.getMatches("2025-02-01", "2025-03-01")
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val matches = parseMatches(response)
                            // Insert matches into the database
                            CoroutineScope(Dispatchers.IO).launch {
                                matchDao.insertMatches(matches)
                            }
                            Log.d("MatchRepository", "Matches updated: $matches")
                        } catch (e: Exception) {
                            Log.e("MatchRepository", "JSON parsing error: ${e.message}")
                        }
                    } else {
                        Log.e("MatchRepository", "API call failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("MatchRepository", "Network error: ${t.message}")
                }
            })
    }

    // Get matches by date from the Room database
    fun getMatchesByDate(date: Date): LiveData<List<Match>> {
        val (startMillis, endMillis) = getStartAndEndOfDayInMillis(date)
        return matchDao.getMatchesByDate(startMillis, endMillis)
    }

    fun getStartAndEndOfDayInMillis(date: Date): Pair<Long, Long> {
        // Convert Date to LocalDateTime
        val localDateTime = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        // Get start of the day (00:00:00)
        val startOfDay = localDateTime.toLocalDate().atStartOfDay()

        // Get end of the day (23:59:59.999999999)
        val endOfDay = localDateTime.toLocalDate().atTime(LocalTime.MAX)

        // Convert LocalDateTime to Instant and then to milliseconds
        val startMillis = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return Pair(startMillis, endMillis)
    }

    // Get a match by ID from the Room database
    fun getMatchById(matchId: Int): LiveData<Match> {
        return matchDao.getMatchById(matchId)
    }

    // Parse the API response into a list of Match objects
    private fun parseMatches(response: Response<String>): MutableList<Match> {
        val jsonObject = JSONObject(response.body()!!)
        val jsonMatches: JSONArray = jsonObject.getJSONArray("matches")
        val matches = mutableListOf<Match>()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        for (i in 0 until jsonMatches.length()) {
            val jsonObject = jsonMatches.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val utcDate: Date = inputFormat.parse(jsonObject.getString("utcDate"))!!
            val homeTeam = jsonObject.getJSONObject("homeTeam").getString("shortName").toString()
            val awayTeam = jsonObject.getJSONObject("awayTeam").getString("shortName").toString()
            // val score = jsonObject.getJSONObject("score").getJSONObject("fullTime")
            // val homeTeamGoals = score.getInt("home")
            // val awayTeamGoals = score.getInt("away")

            matches.add(Match(id, utcDate, homeTeam, awayTeam, 0, 0))
        }

        return matches
    }
}