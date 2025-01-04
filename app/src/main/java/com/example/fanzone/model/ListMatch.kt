package com.example.fanzone.model

import java.util.Date

data class ListMatch (
    val name: String,
    val date: Date,
    val kickoffTime: String,
    val logoResId: Int // Resource ID of the team logo
)