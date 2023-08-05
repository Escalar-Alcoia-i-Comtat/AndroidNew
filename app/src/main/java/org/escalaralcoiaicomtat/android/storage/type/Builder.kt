package org.escalaralcoiaicomtat.android.storage.type

import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

data class Builder(
    val name: String,
    val date: String
): JsonSerializable {
    companion object: JsonSerializer<Builder> {
        override fun fromJson(json: JSONObject): Builder = Builder(
            json.getString("name"),
            json.getString("date")
        )
    }

    override fun toJson(): JSONObject = jsonOf("name" to name, "date" to date)
}
