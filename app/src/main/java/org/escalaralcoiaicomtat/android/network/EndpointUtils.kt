package org.escalaralcoiaicomtat.android.network

import android.net.Uri
import org.escalaralcoiaicomtat.android.BuildConfig
import java.util.UUID

object EndpointUtils {
    private val server: Uri by lazy { Uri.parse(BuildConfig.SERVER) }

    fun getUrl(endpoint: String) = server.buildUpon()
        .let {
            var builder = it
            for(piece in endpoint.split("/")) {
                builder = builder.appendPath(piece)
            }
            builder
        }
        .build()
        .toString()

    fun getFile(uuid: String) = server.buildUpon()
        .appendPath("file")
        .appendPath(uuid)
        .build()
        .toString()

    fun getDownload(uuid: UUID) = server.buildUpon()
        .appendPath("download")
        .appendPath(uuid.toString())
        .build()
        .toString()

}
