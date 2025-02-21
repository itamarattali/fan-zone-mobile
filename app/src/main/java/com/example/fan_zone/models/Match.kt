package com.example.fan_zone.models

import android.location.Location
import java.util.Date

data class Match(
    val id: String,
    val date: Date,
    val location: Location,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamGoals: Int,
    val awayTeamGoals: Int,
)