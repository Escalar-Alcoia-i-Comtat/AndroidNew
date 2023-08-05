package org.escalaralcoiaicomtat.android.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.utils.getJSONObjectOrNull
import org.escalaralcoiaicomtat.android.utils.json
import org.json.JSONObject
import timber.log.Timber

/**
 * Converts the body of the `HttpResponse` object into a `JSONObject`.
 *
 * @return The body of the `HttpResponse` as a `JSONObject`.
 */
suspend fun HttpResponse.bodyAsJson(): JSONObject = bodyAsText().json

/**
 * Runs a GET request to the given endpoint.
 *
 * @param endpoint The URL to make the request to.
 * @param requestBuilder A function for modifying the request.
 * @param responseProcessor Can be used for fetching any data from the response.
 *
 * @throws RequestException If the server doesn't return a successful response.
 */
suspend inline fun get(
    endpoint: String,
    requestBuilder: HttpRequestBuilder.() -> Unit = {},
    responseProcessor: (data: JSONObject?) -> Unit
): HttpResponse {
    Timber.v("GET > $endpoint")

    return ktorHttpClient.get(
        endpoint,
        requestBuilder
    ).apply {
        if (status.value !in 200..299) {
            Timber.e("Server returned an error. Status: $status")
            throw RequestException(status, bodyAsJson())
        }

        val json = bodyAsJson()
        val data = json.getJSONObjectOrNull("data")
        responseProcessor(data)
    }
}

/**
 * Appends the data of [local] to the builder if it's not equal to [server], named [name].
 */
fun <T: Any> FormBuilder.appendUpdate(name: String, local: T?, server: T?) {
    if (local == null || server == null) {
        // TODO: Deletions of data are currently not supported
        return
    }
    if (local != server) {
        when (local) {
            is String -> append(name, local)
            is Number -> append(name, local)
            is Boolean -> append(name, local)
            is ByteArray -> append(name, local)
            else -> throw IllegalArgumentException("Tried to append unsupported type ${local::class.simpleName}")
        }
    }
}
