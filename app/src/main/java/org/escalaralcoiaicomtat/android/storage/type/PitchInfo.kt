package org.escalaralcoiaicomtat.android.storage.type

import android.os.Parcel
import android.os.Parcelable
import org.escalaralcoiaicomtat.android.unit.DistanceUnit
import org.escalaralcoiaicomtat.android.utils.getEnumOrNull
import org.escalaralcoiaicomtat.android.utils.getStringOrNull
import org.escalaralcoiaicomtat.android.utils.getUInt
import org.escalaralcoiaicomtat.android.utils.getUIntOrNull
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

data class PitchInfo(
    val pitch: UInt,
    val gradeValue: GradeValue?,
    val height: UInt?,
    val ending: Ending?,
    val info: EndingInfo? = null,
    val inclination: EndingInclination? = null
): JsonSerializable, Parcelable {
    companion object CREATOR : Parcelable.Creator<PitchInfo>, JsonSerializer<PitchInfo> {
        override fun fromJson(json: JSONObject): PitchInfo = PitchInfo(
            json.getUInt("pitch"),
            json.getStringOrNull("grade")?.let { GradeValue.fromString(it) },
            json.getUIntOrNull("height"),
            json.getEnumOrNull<Ending>("ending"),
            json.getEnumOrNull<EndingInfo>("info"),
            json.getEnumOrNull<EndingInclination>("inclination")
        )

        override fun createFromParcel(parcel: Parcel): PitchInfo = PitchInfo(parcel)

        override fun newArray(size: Int): Array<PitchInfo?> = arrayOfNulls(size)
    }

    val heightUnits: DistanceUnit? = height?.let { DistanceUnit(it.toDouble()) }

    constructor(parcel: Parcel) : this(
        parcel.readLong().toUInt(),
        parcel.readString()?.takeIf { it != "" }?.let(GradeValue::fromString),
        parcel.readLong().takeIf { it >= 0 }?.toUInt(),
        parcel.readString()?.takeIf { it != "" }?.let(Ending::valueOf),
        parcel.readString()?.takeIf { it != "" }?.let(EndingInfo::valueOf),
        parcel.readString()?.takeIf { it != "" }?.let(EndingInclination::valueOf)
    )

    override fun toJson(): JSONObject = jsonOf(
        "pitch" to pitch,
        "grade" to gradeValue,
        "height" to height,
        "ending" to ending,
        "info" to info,
        "inclination" to inclination
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(pitch.toLong())
        dest.writeString(gradeValue?.name ?: "")
        dest.writeLong(height?.toLong() ?: -1)
        dest.writeString(ending?.name ?: "")
        dest.writeString(info?.name ?: "")
        dest.writeString(inclination?.name ?: "")
    }
}
