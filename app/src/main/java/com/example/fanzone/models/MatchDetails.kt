package com.example.fanzone.models

data class MatchDetails(
    val matchId: String,
    val matchTime: String,
    val matchLocation: String,
    val homeTeam: String,
    val awayTeam: String,
    val result: String
)
