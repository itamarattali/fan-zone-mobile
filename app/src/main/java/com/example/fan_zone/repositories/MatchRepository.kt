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
import java.time.format.DateTimeFormatter
import java.util.*

class MatchRepository(context: Context) {

    private val matchDao = MatchDatabase.getDatabase(context).matchDao()

    init {
        fetchMatches()
    }

    private fun fetchMatches() {
        val apiService = RetrofitClient.instance


        val (startDate, endDate) = getDatesToFetch()
        apiService.getMatches(startDate, endDate)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            val matches = parseMatches(response)
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

    fun getMatchesByDate(date: Date): LiveData<List<Match>> {
        val (startMillis, endMillis) = getStartAndEndOfDayInMillis(date)
        return matchDao.getMatchesByDate(startMillis, endMillis)
    }

    fun getStartAndEndOfDayInMillis(date: Date): Pair<Long, Long> {
        val localDateTime = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        val startOfDay = localDateTime.toLocalDate().atStartOfDay()

        val endOfDay = localDateTime.toLocalDate().atTime(LocalTime.MAX)

        val startMillis = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return Pair(startMillis, endMillis)
    }

    fun getMatchById(matchId: Int): LiveData<Match> {
        return matchDao.getMatchById(matchId)
    }

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
            val homeTeam = jsonObject.getJSONObject("homeTeam")
            val awayTeamDisplayName = jsonObject.getJSONObject("awayTeam").getString("shortName").toString()
            val homeTeamDisplayName = homeTeam.getString("shortName").toString()
            val homeTeamImage = homeTeam.getString("crest").toString()
            val score = jsonObject.getJSONObject("score")
            var homeTeamGoals: Int? = null
            var awayTeamGoals: Int? = null
            val winner = score.getString("winner")
            if (winner.toString() != "null"){
                val fullTime = score.getJSONObject("fullTime")
                homeTeamGoals = fullTime.getInt("home")
                awayTeamGoals = fullTime.getInt("away")
            }

            matches.add(Match(id, utcDate, homeTeamDisplayName, awayTeamDisplayName, homeTeamImage, homeTeamGoals, awayTeamGoals))
        }

        return matches
    }

    fun getDatesToFetch(): Pair<String, String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val today = LocalDate.now()
        val startDate = today.minusMonths(1).format(formatter)
        val endDate = today.plusMonths(1).format(formatter)

        return Pair(startDate, endDate)
    }
}