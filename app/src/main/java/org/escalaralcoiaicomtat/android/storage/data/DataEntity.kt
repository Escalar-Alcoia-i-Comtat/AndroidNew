package org.escalaralcoiaicomtat.android.storage.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.escalaralcoiaicomtat.android.exception.remote.RemoteFileNotFound
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.network.get
import org.escalaralcoiaicomtat.android.storage.files.FilesCrate
import org.escalaralcoiaicomtat.android.storage.files.LocalFile
import org.escalaralcoiaicomtat.android.worker.DownloadWorker
import timber.log.Timber
import java.util.UUID

abstract class DataEntity : BaseEntity() {
    abstract val displayName: String
    abstract val image: String
    abstract val isFavorite: Boolean

    val imageUUID: UUID by lazy { UUID.fromString(image.substringBeforeLast('.')) }

    private suspend fun fetchLocalImageInfo(context: Context): RemoteFileInfo? {
        val crate = FilesCrate.getInstance(context)

        val cache = crate.cache(imageUUID)
        if (cache.exists()) {
            return cache.readMeta()
        }

        val permanent = crate.permanent(imageUUID)
        if (permanent.exists()) {
            return permanent.readMeta()
        }

        return null
    }

    private suspend fun fetchImageInfo(): RemoteFileInfo {
        try {
            val endpoint = EndpointUtils.getFile(image)
            var result: RemoteFileInfo? = null
            get(endpoint) { data ->
                requireNotNull(data)

                result = RemoteFileInfo(
                    download = data.getString("download"),
                    filename = data.getString("filename"),
                    size = data.getLong("size"),
                    hash = data.getString("hash"),
                )
            }
            return result!!
        } catch (e: RequestException) {
            if (e.code == RemoteFileNotFound.ERROR_CODE)
                throw RemoteFileNotFound(imageUUID)
            throw e
        }
    }

    suspend fun fetchImage(
        context: Context,
        imageWidth: Int?,
        progress: (suspend (current: Int, max: Int) -> Unit)?
    ): Flow<LocalFile> = channelFlow {
        val local = fetchLocalImageInfo(context)
        if (local != null) {
            val file = local.permanentOrCache(context)
            send(file)
        }

        val info = fetchImageInfo()

        // Check if hashes match
        val remoteFile = info.permanentOrCache(context)
        if (remoteFile.exists() && info.hash == local?.hash) {
            send(remoteFile)
            return@channelFlow
        }

        // If there's no stored version, or hashes do not match, download a new one

        Timber.d("Downloading image for ${this::class.simpleName} $id...")
        val file = DownloadWorker.download(context, info, imageWidth, progress)

        Timber.d("Image for $id downloaded. Decoding image...")
        send(file)
    }
}
