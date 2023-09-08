package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.storage.type.RequiredMaterial
import org.escalaralcoiaicomtat.android.storage.type.SafesCount
import org.escalaralcoiaicomtat.android.utils.getBooleanOrNull
import org.escalaralcoiaicomtat.android.utils.getEnumOrNull
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.getLongOrNull
import org.escalaralcoiaicomtat.android.utils.getSerializableArrayOrNull
import org.escalaralcoiaicomtat.android.utils.getSerializableOrNull
import org.escalaralcoiaicomtat.android.utils.getStringOrNull
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
    override val displayName: String,
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

    override val parentId: Long,

    override val isFavorite: Boolean = false
) : DataEntity(), JsonSerializable {
    companion object : JsonSerializer<Path> {
        override fun fromJson(json: JSONObject): Path = Path(
            json.getLong("id"),
            json.getInstant("timestamp"),

            json.getString("display_name"),
            json.getLong("sketch_id"),

            json.getLongOrNull("height"),
            json.getStringOrNull("grade")?.let(GradeValue::fromString),
            json.getEnumOrNull<Ending>("ending"),

            json.getSerializableArrayOrNull<PitchInfo, PitchInfo.Companion>("pitches"),

            json.getLongOrNull("string_count"),

            json.getLongOrNull("parabolt_count"),
            json.getLongOrNull("buril_count"),
            json.getLongOrNull("piton_count"),
            json.getLongOrNull("spit_count"),
            json.getLongOrNull("tensor_count"),

            json.getBoolean("cracker_required"),
            json.getBoolean("friend_required"),
            json.getBoolean("lanyard_required"),
            json.getBoolean("nail_required"),
            json.getBoolean("piton_required"),
            json.getBoolean("stapes_required"),

            json.getBoolean("show_description"),
            json.getStringOrNull("description"),

            json.getSerializableOrNull<Builder, Builder.Companion>("builder"),
            json.getSerializableArrayOrNull<Builder, Builder.Companion>("re_builder"),

            json.getLong("sector_id"),

            json.getBooleanOrNull("is_favorite") ?: false
        )
    }

    @get:Ignore
    override val pluralRes: Int get() = throw UnsupportedOperationException("Paths don't have any children")

    @Ignore
    override val childrenTitleRes: Int = -1

    @Ignore
    val ropeLength: Long? = height?.let {
        val minLength = it * 2
        val standardLengths = setOf<Long>(30, 40, 50, 60, 70, 80)
        for (length in standardLengths) {
            if (minLength < length) {
                return@let length
            }
        }
        minLength
    }

    /**
     * Checks whether any of the count variables ([paraboltCount], [burilCount], [pitonCount],
     * [spitCount], [tensorCount]) is not null.
     */
    @Ignore
    val anyCount: Boolean = paraboltCount != null || burilCount != null || pitonCount != null ||
        spitCount != null || tensorCount != null

    @Ignore
    val parabolts: SafesCount? = paraboltCount
        ?.takeIf { it > 0 }
        ?.let { SafesCount(it, R.plurals.safe_type_parabolt, R.string.safe_type_parabolt) }

    @Ignore
    val burils: SafesCount? = burilCount
        ?.takeIf { it > 0 }
        ?.let { SafesCount(it, R.plurals.safe_type_buril, R.string.safe_type_buril) }

    @Ignore
    val pitons: SafesCount? = pitonCount
        ?.takeIf { it > 0 }
        ?.let { SafesCount(it, R.plurals.safe_type_piton, R.string.safe_type_piton) }

    @Ignore
    val spits: SafesCount? = spitCount
        ?.takeIf { it > 0 }
        ?.let { SafesCount(it, R.plurals.safe_type_spit, R.string.safe_type_spit) }

    @Ignore
    val tensors: SafesCount? = tensorCount
        ?.takeIf { it > 0 }
        ?.let { SafesCount(it, R.plurals.safe_type_tensor, R.string.safe_type_tensor) }

    /**
     * Returns true if any of [crackerRequired], [friendRequired], [lanyardRequired], [nailRequired],
     * [pitonRequired] or [stapesRequired] is true.
     */
    @Ignore
    val anyRequired: Boolean = crackerRequired || friendRequired || lanyardRequired ||
        nailRequired || pitonRequired || stapesRequired

    @Ignore
    val cracker: RequiredMaterial? = RequiredMaterial(R.string.required_type_cracker).takeIf { crackerRequired }

    @Ignore
    val friend: RequiredMaterial? = RequiredMaterial(R.string.required_type_friend).takeIf { friendRequired }

    @Ignore
    val lanyard: RequiredMaterial? = RequiredMaterial(R.string.required_type_lanyard).takeIf { lanyardRequired }

    @Ignore
    val nail: RequiredMaterial? = RequiredMaterial(R.string.required_type_nail).takeIf { nailRequired }

    @Ignore
    val piton: RequiredMaterial? = RequiredMaterial(R.string.required_type_piton).takeIf { pitonRequired }

    @Ignore
    val stapes: RequiredMaterial? = RequiredMaterial(R.string.required_type_stapes).takeIf { stapesRequired }

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

        "sector_id" to parentId,
        
        "is_favorite" to isFavorite
    )
}
