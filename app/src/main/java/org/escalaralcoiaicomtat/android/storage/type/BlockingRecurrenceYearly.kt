package org.escalaralcoiaicomtat.android.storage.type

import org.escalaralcoiaicomtat.android.utils.getEnum
import org.escalaralcoiaicomtat.android.utils.getUShort
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.time.Month

data class BlockingRecurrenceYearly(
    val fromDay: UShort,
    val fromMonth: Month,
    val toDay: UShort,
    val toMonth: Month
): JsonSerializable {
    companion object: JsonSerializer<BlockingRecurrenceYearly> {
        override fun fromJson(json: JSONObject): BlockingRecurrenceYearly = BlockingRecurrenceYearly(
            json.getUShort("from_day"),
            json.getEnum("from_month"),
            json.getUShort("to_day"),
            json.getEnum("to_month")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "from_day" to fromDay,
        "from_month" to fromMonth,
        "to_day" to toDay,
        "to_month" to toMonth
    )
}
