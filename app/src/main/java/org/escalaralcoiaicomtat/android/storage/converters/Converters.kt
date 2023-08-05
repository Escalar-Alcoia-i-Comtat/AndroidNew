package org.escalaralcoiaicomtat.android.storage.converters

import androidx.room.TypeConverter
import org.escalaralcoiaicomtat.android.storage.type.BlockingRecurrenceYearly
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.utils.json

class Converters {
    @TypeConverter
    fun toLatLng(value: String?): LatLng? = value?.let { LatLng.fromJson(value.json) }

    @TypeConverter
    fun fromLatLng(value: LatLng?): String? = value?.toJson()?.toString()

    @TypeConverter
    fun toGrade(value: String?): GradeValue? = value?.let(GradeValue::fromString)

    @TypeConverter
    fun fromGrade(value: GradeValue?): String? = value?.name

    @TypeConverter
    fun toBuilder(value: String?): Builder? = value?.json?.let(Builder::fromJson)

    @TypeConverter
    fun fromBuilder(value: Builder?): String? = value?.toJson()?.toString()

    @TypeConverter
    fun toBlockingRecurrenceYearly(value: String?): BlockingRecurrenceYearly? =
        value?.json?.let(BlockingRecurrenceYearly::fromJson)

    @TypeConverter
    fun fromBlockingRecurrenceYearly(value: BlockingRecurrenceYearly?): String? =
        value?.toJson()?.toString()
}
