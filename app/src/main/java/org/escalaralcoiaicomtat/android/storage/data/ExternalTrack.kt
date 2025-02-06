package org.escalaralcoiaicomtat.android.storage.data

import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject

data class ExternalTrack(
    val type: Type,
    val url: String
): JsonSerializable {
    companion object : JsonSerializer<ExternalTrack> {
        override fun fromJson(json: JSONObject): ExternalTrack {
            return ExternalTrack(
                Type.valueOf(json.getString("type")),
                json.getString("url")
            )
        }
    }

    override fun toJson(): JSONObject {
        return jsonOf(
            "type" to type.name,
            "url" to url
        )
    }

    enum class Type {
        Wikiloc
    }
}
