package de.bauhaupt.urlaubsschein.data.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

object Converters {

    // Instant -> Long (Epoch Millis)
    @TypeConverter
    @JvmStatic
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    // Long -> Instant
    @TypeConverter
    @JvmStatic
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    // LocalDate -> String (ISO-8601, z. B. "2026-08-12")
    @TypeConverter
    @JvmStatic
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    // String -> LocalDate
    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}