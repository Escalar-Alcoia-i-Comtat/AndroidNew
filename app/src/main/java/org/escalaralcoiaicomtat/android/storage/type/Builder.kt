package org.escalaralcoiaicomtat.android.storage.type

import org.escalaralcoiaicomtat.android.utils.getStringOrNull
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

data class Builder(
    val name: String? = null,
    val date: String? = null
): JsonSerializable {
    companion object: JsonSerializer<Builder> {
        override fun fromJson(json: JSONObject): Builder = Builder(
            json.getStringOrNull("name"),
            json.getStringOrNull("date")
        )
    }

    override fun toJson(): JSONObject = jsonOf("name" to name, "date" to date)
}
