package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.storage.type.BlockingRecurrenceYearly
import org.escalaralcoiaicomtat.android.storage.type.BlockingTypes
import org.escalaralcoiaicomtat.android.utils.getEnum
import org.escalaralcoiaicomtat.android.utils.getInstant
import org.escalaralcoiaicomtat.android.utils.getJSONObjectOrNull
import org.escalaralcoiaicomtat.android.utils.getStringOrNull
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity(tableName = "blocking")
data class Blocking(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0L,
    override val timestamp: Instant,
    val type: BlockingTypes,
    val recurrence: BlockingRecurrenceYearly?,
    val endDate: ZonedDateTime?,
    val pathId: Long
) : BaseEntity(), JsonSerializable {
    companion object: JsonSerializer<Blocking> {
        override fun fromJson(json: JSONObject): Blocking = Blocking(
            json.getLong("id"),
            json.getInstant("timestamp"),
            json.getEnum("type"),
            json.getJSONObjectOrNull("recurrence")
                ?.let(BlockingRecurrenceYearly::fromJson),
            json.getStringOrNull("end_date")
                ?.let(LocalDateTime::parse)
                ?.atZone(ZoneId.systemDefault()),
            json.getLong("path_id")
        )
    }

    fun shouldDisplay(): Boolean {
        val now = LocalDate.now()
        val nowYear = Year.now()

        if (recurrence != null) {
            val from = recurrence.from
            val to = recurrence.to

            val year = if (from.atYear(nowYear.value).isBefore(now)) {
                // If date is before now, next year applies
                nowYear.plusYears(1)
            } else {
                nowYear
            }

            val fromDate = from.atYear(year.value)
            val toDate = if (to.isBefore(from)) {
                // to is next year
                to.atYear(year.plusYears(1).value)
            } else {
                to.atYear(year.value)
            }
            return now.isAfter(fromDate) && now.isBefore(toDate)
        } else if (endDate != null) {
            return now.isBefore(endDate.toLocalDate())
        } else {
            return true
        }
    }

    override fun toJson(): JSONObject = jsonOf(
        "id" to id,
        "timestamp" to timestamp,
        "type" to type,
        "recurrence" to recurrence,
        "end_date" to endDate?.toString(),
        "path_id" to pathId
    )
}
