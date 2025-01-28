package com.example.fan_zone.models

data class MatchDetails(
    val matchId: String,
    val matchTime: String,
    val matchLocation: String,
    val homeTeam: String,
    val awayTeam: String,
    val result: String
)