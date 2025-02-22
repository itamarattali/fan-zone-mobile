package com.example.fan_zone.models


import java.util.Date

data class Match(
    val id: Int,
    val date: Date,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamGoals: Int?,
    val awayTeamGoals: Int?,
)