package com.example.fan_zone.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fan_zone.models.Match

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<Match>)

    @Query("SELECT * FROM matches WHERE date > :startOfDay AND date < :endOfDay")
    fun getMatchesByDate(startOfDay: Long, endOfDay: Long): LiveData<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: Int): LiveData<Match>?

    @Query("SELECT * FROM matches")
    fun getAllMatches(): LiveData<List<Match>>
}