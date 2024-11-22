package org.escalaralcoiaicomtat.android.network

import android.net.Uri
import org.escalaralcoiaicomtat.android.BuildConfig
import org.escalaralcoiaicomtat.android.utils.letIf
import java.util.UUID

object EndpointUtils {
    private val server: Uri by lazy { Uri.parse("${BuildConfig.PROTOCOL}://${BuildConfig.HOSTNAME}") }

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
        .appendPath("files")
        .appendPath(uuid)
        .build()
        .toString()

    fun getDownload(uuid: UUID, width: Int? = null) = server.buildUpon()
        .appendPath("download")
        .appendPath(uuid.toString())
        .letIf(width != null) { it.appendQueryParameter("width", width!!.toString()) }
        .build()
        .toString()

}
