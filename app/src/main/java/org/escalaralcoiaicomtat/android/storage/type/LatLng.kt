package org.escalaralcoiaicomtat.android.storage.type

import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

/**
 * Represents geographic coordinates in latitude and longitude.
 *
 * This data class provides a convenient way to store and manipulate latitude and longitude values.
 * It implements the `JsonSerializable` interface, allowing instances of `LatLng` to be serialized and deserialized from
 * JSON.
 *
 * @property latitude The latitude value of the geographic coordinates.
 * @property longitude The longitude value of the geographic coordinates.
 *
 * @constructor Creates a new `LatLng` instance.
 *
 * @param latitude The latitude value of the geographic coordinates.
 * @param longitude The longitude value of the geographic coordinates.
 */
data class LatLng(
    val latitude: Double,
    val longitude: Double
) : JsonSerializable {
    companion object : JsonSerializer<LatLng> {
        override fun fromJson(json: JSONObject): LatLng = LatLng(
            json.getDouble("latitude"),
            json.getDouble("longitude")
        )
    }

    constructor(latitude: String, longitude: String) : this(
        latitude.toDouble(),
        longitude.toDouble()
    )

    override fun toJson(): JSONObject = jsonOf(
        "latitude" to latitude,
        "longitude" to longitude
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LatLng

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }
}
