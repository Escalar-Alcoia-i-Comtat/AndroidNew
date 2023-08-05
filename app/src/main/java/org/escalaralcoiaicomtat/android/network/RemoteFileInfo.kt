package org.escalaralcoiaicomtat.android.network

import android.content.Context
import org.escalaralcoiaicomtat.android.storage.files.FilesCrate
import org.escalaralcoiaicomtat.android.utils.jsonOf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONObject
import java.util.UUID

data class RemoteFileInfo(
    val download: String,
    val filename: String,
    val size: Long,
    val hash: String
) : JsonSerializable {
    companion object : JsonSerializer<RemoteFileInfo> {
        override fun fromJson(json: JSONObject): RemoteFileInfo = RemoteFileInfo(
            json.getString("download"),
            json.getString("filename"),
            json.getLong("size"),
            json.getString("hash")
        )
    }

    val uuid: UUID = download
        .substringAfterLast('/')
        .substringBeforeLast('.')
        .let(UUID::fromString)

    fun cache(context: Context) = FilesCrate.getInstance(context).cache(uuid)

    fun permanent(context: Context) = FilesCrate.getInstance(context).permanent(uuid)

    /**
     * Returns the value of [permanent] if the file exists, or [cache] otherwise.
     */
    fun permanentOrCache(context: Context) =
        permanent(context).takeIf { it.exists() } ?: cache(context)

    override fun toJson(): JSONObject = jsonOf(
        "download" to download,
        "filename" to filename,
        "size" to size,
        "hash" to hash
    )
}
