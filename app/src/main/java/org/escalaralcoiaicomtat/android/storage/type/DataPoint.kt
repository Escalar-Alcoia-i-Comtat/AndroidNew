package org.escalaralcoiaicomtat.android.storage.type

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.form.PointOption
import org.escalaralcoiaicomtat.android.utils.json
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

/**
 * Represents a data point with location, label, and icon information.
 *
 * @property location The location of the data point.
 * @property label The label associated with the data point.
 * @property icon The icon associated with the data point.
 */
data class DataPoint(
    val location: LatLng,
    val label: String,
    val icon: String
): JsonSerializable, Parcelable {
    companion object CREATOR: JsonSerializer<DataPoint>, Parcelable.Creator<DataPoint> {
        override fun fromJson(json: JSONObject): DataPoint = DataPoint(
            LatLng.fromJson(json.getJSONObject("location")),
            json.getString("label"),
            json.getString("icon")
        )

        override fun createFromParcel(parcel: Parcel): DataPoint {
            return DataPoint(parcel)
        }

        override fun newArray(size: Int): Array<DataPoint?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!.json.let(LatLng::fromJson),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun toJson(): JSONObject = jsonOf(
        "location" to location,
        "label" to label,
        "icon" to icon
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(location.toString())
        parcel.writeString(label)
        parcel.writeString(icon)
    }

    override fun describeContents(): Int {
        return 0
    }
}

object PointOptions {
    val Default = PointOption("default", Icons.Outlined.LocationOn, R.string.icon_label_default)
    val Parking = PointOption("parking", Icons.Outlined.LocalParking, R.string.icon_label_parking)
    val Park = PointOption("park", Icons.Outlined.Park, R.string.icon_label_park)
    val Water = PointOption("water", Icons.Outlined.WaterDrop, R.string.icon_label_water)
    val Pool = PointOption("pool", Icons.Outlined.Pool, R.string.icon_label_pool)
    val Restaurant = PointOption("restaurant", Icons.Outlined.Restaurant, R.string.icon_label_restaurant)
    val Hotel = PointOption("hotel", Icons.Outlined.Hotel, R.string.icon_label_hotel)

    val All = arrayOf(
        Default, Parking, Park, Water, Pool, Restaurant, Hotel
    )

    fun valueOf(key: String) = All.find { it.key == key }
}
