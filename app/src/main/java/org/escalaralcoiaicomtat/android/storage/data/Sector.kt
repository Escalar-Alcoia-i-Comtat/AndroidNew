package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.utils.getBooleanOrNull
import org.escalaralcoiaicomtat.android.utils.getEnum
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.getLongOrNull
import org.escalaralcoiaicomtat.android.utils.getSerializable
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.time.Instant

@Entity(tableName = "sectors")
@Suppress("LongParameterList")
data class Sector(
    @PrimaryKey
    override val id: Long,
    override val timestamp: Instant,
    override val displayName: String,
    val kidsApt: Boolean,
    val sunTime: SunTime,
    val walkingTime: Long?,
    override val image: String,
    val point: LatLng?,
    val weight: String,
    val zoneId: Long,
    override val isFavorite: Boolean = false
) : DataEntity(), JsonSerializable {
    companion object: JsonSerializer<Sector> {
        override fun fromJson(json: JSONObject): Sector = Sector(
            json.getLong("id"),
            json.getInstant("timestamp"),
            json.getString("display_name"),
            json.getBoolean("kids_apt"),
            json.getEnum("sun_time"),
            json.getLongOrNull("walking_time"),
            json.getString("image"),
            json.getSerializable<LatLng, LatLng.Companion>("point"),
            json.getString("weight"),
            json.getLong("zone_id"),
            json.getBooleanOrNull("is_favorite") ?: false
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp,
        "display_name" to displayName,
        "kids_apt" to kidsApt,
        "sun_time" to sunTime,
        "walking_time" to walkingTime,
        "image" to image,
        "point" to point,
        "weight" to weight,
        "zone_id" to zoneId,
        "is_favorite" to isFavorite
    )
}
