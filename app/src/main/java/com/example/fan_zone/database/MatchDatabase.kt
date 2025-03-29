package com.example.fan_zone.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.fan_zone.database.converters.DateConverter
import com.example.fan_zone.models.Match

@Database(entities = [Match::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class MatchDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: MatchDatabase? = null

        fun getDatabase(context: Context): MatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MatchDatabase::class.java,
                    "match_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}