package com.example.fan_zone.database.converters

import androidx.room.TypeConverter
import java.util.Date

object DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return if (dateLong == null) null else Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }
}