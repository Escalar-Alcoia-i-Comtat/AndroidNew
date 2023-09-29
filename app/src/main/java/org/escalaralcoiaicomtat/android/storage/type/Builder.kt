package org.escalaralcoiaicomtat.android.storage.type

import android.os.Parcel
import android.os.Parcelable
import org.escalaralcoiaicomtat.android.utils.getStringOrNull
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

data class Builder(
    val name: String? = null,
    val date: String? = null
) : JsonSerializable, Parcelable {
    companion object CREATOR : JsonSerializer<Builder>, Parcelable.Creator<Builder> {
        override fun fromJson(json: JSONObject): Builder = Builder(
            json.getStringOrNull("name"),
            json.getStringOrNull("date")
        )

        override fun createFromParcel(parcel: Parcel): Builder = Builder(parcel)

        override fun newArray(size: Int): Array<Builder?> = arrayOfNulls(size)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().takeIf { it != "" },
        parcel.readString().takeIf { it != "" }
    )

    override fun toJson(): JSONObject = jsonOf("name" to name, "date" to date)

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name ?: "")
        parcel.writeString(date ?: "")
    }
}
