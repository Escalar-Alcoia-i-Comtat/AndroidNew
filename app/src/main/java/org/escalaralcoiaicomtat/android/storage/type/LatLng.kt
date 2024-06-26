package org.escalaralcoiaicomtat.android.storage.type

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.escalaralcoiaicomtat.android.utils.UriUtils.viewIntent
import org.escalaralcoiaicomtat.android.utils.canBeResolved
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

    val uri: Uri = Uri.parse("geo:$latitude,$longitude")

    private fun gmapsUri(label: String): Uri {
        val builder = StringBuilder(uri.toString())
        builder.append("?q=$latitude,$longitude")
        val labelValue = label.replace(' ', '+')
        builder.append("($labelValue)")
        return Uri.parse(builder.toString())
    }

    private fun sygicUri(label: String, type: String = "walk"): Uri {
        return Uri.parse("com.sygic.aura://coordinateaddr|$longitude,$latitude|$label|$type")
    }

    fun intent(context: Context, label: String, sygicType: String = "walk"): Intent? {
        val googleMapsIntent = gmapsUri(label)
            .viewIntent
            .setPackage("com.google.android.apps.maps")
            .takeIf { it.canBeResolved(context) }
        val sygicIntent = sygicUri(sygicType)
            .viewIntent
            .setPackage("com.sygic.aura")
            .takeIf { it.canBeResolved(context) }

        return sygicIntent ?: googleMapsIntent
    }

    override fun toString(): String = toJson().toString()

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
