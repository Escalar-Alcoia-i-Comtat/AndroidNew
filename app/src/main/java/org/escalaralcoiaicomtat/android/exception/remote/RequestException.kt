package org.escalaralcoiaicomtat.android.exception.remote

import io.ktor.http.HttpStatusCode
import org.escalaralcoiaicomtat.android.utils.getStringOrNull
import org.json.JSONObject

class RequestException(
    val status: HttpStatusCode,
    responseJson: JSONObject
) : RuntimeException(
    responseJson.getJSONObject("error").getStringOrNull("message")
) {
    private val errorJson: JSONObject = responseJson.getJSONObject("error")

    val extra: String? = errorJson.getStringOrNull("extra")

    val code: Int = errorJson.getInt("code")
}
