package org.escalaralcoiaicomtat.android.storage.converters

import androidx.room.TypeConverter
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.utils.jsonArray
import org.escalaralcoiaicomtat.android.utils.map
import org.escalaralcoiaicomtat.android.utils.serialize
import org.escalaralcoiaicomtat.android.utils.toJson
import org.json.JSONArray

@Suppress("TooManyFunctions")
class ListConverters {
    @TypeConverter
    fun toDataPointList(value: String?): List<DataPoint>? = value?.jsonArray?.serialize(DataPoint)

    @TypeConverter
    fun fromDataPointList(value: List<DataPoint>?): String? = value?.toJson()?.toString()

    @TypeConverter
    fun toPitchInfoList(value: String?): List<PitchInfo>? = value?.jsonArray?.serialize(PitchInfo)

    @TypeConverter
    fun fromPitchInfoList(value: List<PitchInfo>?): String? = value?.toJson()?.toString()

    @TypeConverter
    fun toBuilderList(value: String?): List<Builder>? = value?.jsonArray?.serialize(Builder)

    @TypeConverter
    fun fromBuilderList(value: List<Builder>?): String? = value?.toJson()?.toString()

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.jsonArray?.map { getString(it) }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value
        ?.let { list ->
            JSONArray().apply {
                list.forEach { put(it) }
            }
        }
        ?.toString()
}
