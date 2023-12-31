package org.escalaralcoiaicomtat.android.storage.data

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.R
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
    override val image: String
) : ImageEntity(), JsonSerializable, Parcelable {
    companion object CREATOR : JsonSerializer<Area>, Parcelable.Creator<Area> {
        override fun fromJson(json: JSONObject): Area = Area(
            json.getLong("id"),
            json.getInstant("timestamp"),
            json.getString("display_name"),
            json.getString("web_url").let(Uri::parse),
            json.getString("image")
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
        parcel.readString()!!
    )

    override val parentId: Long
        get() = throw UnsupportedOperationException("Areas don't have parents.")

    @Ignore
    override val pluralRes: Int = R.plurals.zone_count

    @Ignore
    override val childrenTitleRes: Int = R.string.list_area_children

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp,
        "display_name" to displayName,
        "web_url" to webUrl,
        "image" to image
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(timestamp.toEpochMilli())
        parcel.writeString(displayName)
        parcel.writeParcelable(webUrl, 0)
        parcel.writeString(image)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Area

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (displayName != other.displayName) return false
        if (webUrl != other.webUrl) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + webUrl.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }
}
