package org.escalaralcoiaicomtat.android.storage.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.utils.getBooleanOrNull
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.getJSONObjectOrNull
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.escalaralcoiaicomtat.android.utils.serialize
import org.json.JSONObject
import java.time.Instant

@Entity(tableName = "zones")
@Suppress("LongParameterList")
data class Zone(
    @PrimaryKey
    override val id: Long,
    override val timestamp: Instant,
    override val displayName: String,
    val webUrl: Uri,
    override val image: String,
    val kmz: String,
    val point: LatLng?,
    val points: List<DataPoint>,
    val areaId: Long,
    override val isFavorite: Boolean = false
) : ImageEntity(), JsonSerializable {
    companion object: JsonSerializer<Zone> {
        override fun fromJson(json: JSONObject): Zone = Zone(
            json.getLong("id"),
            json.getInstant("timestamp"),
            json.getString("display_name"),
            json.getString("web_url").let(Uri::parse),
            json.getString("image"),
            json.getString("kmz"),
            json.getJSONObjectOrNull("point")?.let(LatLng::fromJson),
            json.getJSONArray("points").serialize(DataPoint),
            json.getLong("area_id"),
            json.getBooleanOrNull("is_favorite") ?: false
        )
    }

    @Ignore
    override val pluralRes: Int = R.plurals.zone_count

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp,
        "display_name" to displayName,
        "web_url" to webUrl,
        "image" to image,
        "kmz" to kmz,
        "point" to point,
        "points" to points,
        "area_id" to areaId,
        "is_favorite" to isFavorite
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Zone

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (displayName != other.displayName) return false
        if (webUrl != other.webUrl) return false
        if (image != other.image) return false
        if (kmz != other.kmz) return false
        if (point != other.point) return false
        if (points != other.points) return false
        if (areaId != other.areaId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + webUrl.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + kmz.hashCode()
        result = 31 * result + (point?.hashCode() ?: 0)
        result = 31 * result + points.hashCode()
        result = 31 * result + areaId.hashCode()
        return result
    }
}
