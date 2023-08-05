package org.escalaralcoiaicomtat.android.storage.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZonedDateTime

class JavaConverters {
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toZonedDateTime(value: String?): ZonedDateTime? = value?.let(ZonedDateTime::parse)

    @TypeConverter
    fun fromZonedDateTime(value: ZonedDateTime?): String? = value?.toString()
}
