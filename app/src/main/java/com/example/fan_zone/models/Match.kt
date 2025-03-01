package com.example.fan_zone.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val date: Date,
    val homeTeam: String,
    val awayTeam: String,
    val matchImage: String,
    val homeTeamGoals: Int?,
    val awayTeamGoals: Int?
)