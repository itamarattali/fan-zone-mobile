package com.example.fanzone.model

import com.example.fanzone.R
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ListMatch (
    val name: String,
    val date: Date,
    val kickoffTime: String,
    val logoResId: Int // Resource ID of the team logo
)

fun parseMatches(jsonArray: JSONArray): MutableList<ListMatch> {
    val matches = mutableListOf<ListMatch>()
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val homeTeam = jsonObject.getJSONObject("homeTeam")
        val awayTeam = jsonObject.getJSONObject("awayTeam")

        val matchName = "${homeTeam.getString("shortName")} vs ${awayTeam.getString("shortName")}"
        val utcDate: Date = inputFormat.parse(jsonObject.getString("utcDate"))!!

        val kickoffTime = timeFormat.format(utcDate)
        val crestUrl = homeTeam.getString("crest") // URL of the home team logo

        matches.add(ListMatch(matchName, utcDate, kickoffTime, R.drawable.logo_manutd))
    }

    return matches
}
