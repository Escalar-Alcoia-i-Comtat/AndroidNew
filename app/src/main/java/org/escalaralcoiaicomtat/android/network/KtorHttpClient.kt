package org.escalaralcoiaicomtat.android.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import timber.log.Timber

val ktorHttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json()
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Timber.v("Logger Ktor => %s", message)
            }

        }
        level = LogLevel.ALL
    }

    install(ResponseObserver) {
        onResponse { response ->
            Timber.d("HTTP status: %d", response.status.value)
        }
    }

    install(DefaultRequest) {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }
}
