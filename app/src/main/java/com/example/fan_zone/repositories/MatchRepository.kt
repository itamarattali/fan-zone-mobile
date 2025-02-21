package com.example.fan_zone.repositories

import com.example.fan_zone.models.Match
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class MatchRepository {
    private val db = FirebaseFirestore.getInstance()
    private val matchCollection = db.collection("matches")

    suspend fun getMatchById(matchId: String): Match? {
        return try {
            val document = matchCollection.document(matchId).get().await()
            document.toObject(Match::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMatchesByDate(date: Date): List<Match> {
        return try {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the start of the next day
            val endOfDay = calendar.time

            val snapshot = matchCollection
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", endOfDay)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.toObjects(Match::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
