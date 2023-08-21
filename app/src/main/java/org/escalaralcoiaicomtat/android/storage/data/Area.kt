package org.escalaralcoiaicomtat.android.storage.data

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.utils.getBooleanOrNull
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.time.Instant

/**
 * Represents the data structure of an Area, which contains Zones.
 */
@Entity(tableName = "areas")
data class Area(
    @PrimaryKey
    override val id: Long = 0L,
    override val timestamp: Instant,
    override val displayName: String,
    val webUrl: Uri,
    override val image: String,
    override val isFavorite: Boolean = false,
) : DataEntity(), JsonSerializable, Parcelable {
    companion object CREATOR : JsonSerializer<Area>, Parcelable.Creator<Area> {
        override fun fromJson(json: JSONObject): Area = Area(
            json.getLong("id"),
            json.getInstant("timestamp"),
            json.getString("display_name"),
            json.getString("web_url").let(Uri::parse),
            json.getString("image").let { it.substring(0, it.indexOf('.')) },
            json.getBooleanOrNull("is_favorite") ?: false
        )

        override fun createFromParcel(parcel: Parcel): Area = Area(parcel)

        override fun newArray(size: Int): Array<Area?> = arrayOfNulls(size)
    }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong().let(Instant::ofEpochMilli),
        parcel.readString()!!,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(Uri::class.java.classLoader, Uri::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            parcel.readParcelable(Uri::class.java.classLoader)!!
        },
        parcel.readString()!!,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.readBoolean()
        } else {
            parcel.readByte().toInt() == 1
        }
    )

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp,
        "display_name" to displayName,
        "web_url" to webUrl,
        "image" to image,
        "is_favorite" to isFavorite
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(timestamp.toEpochMilli())
        parcel.writeString(displayName)
        parcel.writeParcelable(webUrl, 0)
        parcel.writeString(image)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(isFavorite)
        } else {
            parcel.writeByte(if (isFavorite) 1 else 0)
        }
    }
}
