package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.utils.getEnum
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.getSerializable
import org.escalaralcoiaicomtat.android.utils.getSerializableArray
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.time.Instant

@Entity(tableName = "paths")
@Suppress("LongParameterList")
data class Path(
    @PrimaryKey
    override val id: Long = 0L,
    override val timestamp: Instant,
    val displayName: String,
    val sketchId: Long,

    val height: Long?,
    val grade: GradeValue?,
    val ending: Ending?,

    val pitches: List<PitchInfo>?,

    val stringCount: Long?,

    val paraboltCount: Long?,
    val burilCount: Long?,
    val pitonCount: Long?,
    val spitCount: Long?,
    val tensorCount: Long?,

    val crackerRequired: Boolean,
    val friendRequired: Boolean,
    val lanyardRequired: Boolean,
    val nailRequired: Boolean,
    val pitonRequired: Boolean,
    val stapesRequired: Boolean,

    val showDescription: Boolean,
    val description: String?,

    val builder: Builder?,
    val reBuilder: List<Builder>?,

    val sectorId: Long
) : BaseEntity(), JsonSerializable {
    companion object : JsonSerializer<Path> {
        override fun fromJson(json: JSONObject): Path = Path(
            json.getLong("id"),
            json.getInstant("timestamp"),

            json.getString("display_name"),
            json.getLong("sketch_id"),

            json.getLong("height"),
            json.getString("grade").let(GradeValue::fromString),
            json.getEnum<Ending>("ending"),

            json.getSerializableArray<PitchInfo, PitchInfo.Companion>("pitches"),

            json.getLong("string_count"),

            json.getLong("parabolt_count"),
            json.getLong("buril_count"),
            json.getLong("piton_count"),
            json.getLong("spit_count"),
            json.getLong("tensor_count"),

            json.getBoolean("cracker_required"),
            json.getBoolean("friend_required"),
            json.getBoolean("lanyard_required"),
            json.getBoolean("nail_required"),
            json.getBoolean("piton_required"),
            json.getBoolean("stapes_required"),

            json.getBoolean("show_description"),
            json.getString("description"),

            json.getSerializable<Builder, Builder.Companion>("builder"),
            json.getSerializableArray<Builder, Builder.Companion>("re_builder"),

            json.getLong("sector_id")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp.toEpochMilli(),

        "display_name" to displayName,
        "sketch_id" to sketchId,

        "height" to height,
        "grade" to grade,
        "ending" to ending,

        "pitches" to pitches,

        "string_count" to stringCount,

        "parabolt_count" to paraboltCount,
        "buril_count" to burilCount,
        "piton_count" to pitonCount,
        "spit_count" to spitCount,
        "tensor_count" to tensorCount,

        "cracker_required" to crackerRequired,
        "friend_required" to friendRequired,
        "lanyard_required" to lanyardRequired,
        "nail_required" to nailRequired,
        "piton_required" to pitonRequired,
        "stapes_required" to stapesRequired,

        "show_description" to showDescription,
        "description" to description,

        "builder" to builder,
        "re_builder" to reBuilder,

        "sector_id" to sectorId
    )
}
