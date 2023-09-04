package org.escalaralcoiaicomtat.android.storage.files

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.Lifecycle
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.BuildConfig
import org.escalaralcoiaicomtat.android.exception.remote.RemoteFileNotFoundException
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.network.get
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.json.JSONException
import timber.log.Timber
import java.util.UUID

/**
 * Provides an interface between a local file and a remote one. As well, provides the option to
 * store files between cache and a permanent storage.
 */
data class SynchronizedFile(
    val uuid: UUID,
    val cache: LocalFile,
    val permanent: LocalFile
) {
    companion object {
        fun create(context: Context, uuid: UUID): SynchronizedFile {
            val crate = FilesCrate.getInstance(context)

            return SynchronizedFile(
                uuid,
                crate.cache(uuid),
                crate.permanent(uuid)
            )
        }
    }

    private val fileUpdatedListeners: LinkedHashMap<LocalFile, List<(LocalFile) -> Unit>> = LinkedHashMap()

    /**
     * Will be [permanent] if it exists, or [cache] otherwise.
     */
    private val targetFile get() = if (permanent.exists()) permanent else cache

    @Synchronized
    private fun startListening(file: LocalFile, listener: (LocalFile) -> Unit) {
        val list = fileUpdatedListeners.getOrDefault(file, emptyList()).toMutableList()
        list.add(listener)
        fileUpdatedListeners[file] = list
    }

    @Synchronized
    private fun stopListening(file: LocalFile, listener: (LocalFile) -> Unit) {
        val list = fileUpdatedListeners.getOrDefault(file, emptyList()).toMutableList()
        list.remove(listener)
        fileUpdatedListeners[file] = list
    }

    /**
     * Reads the contents of the local meta file, first permanent, and if it doesn't exist, cache.
     *
     * @return The stored meta info, or null if none.
     */
    suspend fun localMeta(): RemoteFileInfo? {
        if (permanent.exists()) return permanent.readMeta()

        if (cache.exists()) return cache.readMeta()

        return null
    }

    /**
     * Runs a network request to get the meta data of this file from the server.
     *
     * @throws RemoteFileNotFoundException If there isn't any file with [uuid] on the server.
     * @throws RequestException If there's any other error on the request.
     * @throws JSONException If there's any error of format in the response.
     * @throws AssertionError If the response doesn't have any data.
     */
    suspend fun remoteMeta(): RemoteFileInfo {
        try {
            val endpoint = EndpointUtils.getFile(uuid.toString())
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
            if (e.code == RemoteFileNotFoundException.ERROR_CODE)
                throw RemoteFileNotFoundException(uuid)
            throw e
        }
    }

    @WorkerThread
    fun read(lifecycle: Lifecycle): Flow<ByteArray?> = channelFlow {
        val fileUpdatedListener: (LocalFile) -> Unit = { file ->
            if (file.exists()) {
                Timber.d("Got update to $file. Sending update...")
                val bytes = file.inputStream().use { it.readBytes() }
                runBlocking { send(bytes) }
            } else {
                Timber.w("Could not read $file. It doesn't exist.")
                runBlocking { send(null) }
            }
        }

        withContext(Dispatchers.Main) {
            // Start watching the cache for changes
            cache.watch(
                lifecycle,
                object : FileUpdateListener(cache) {
                    override fun onCreate(file: LocalFile) {
                        fileUpdatedListener(file)
                    }

                    override fun onModify(file: LocalFile) {
                        fileUpdatedListener(file)
                    }

                    override fun onDelete(file: LocalFile) {
                        fileUpdatedListener(file)
                    }
                }
            )
            permanent.watch(
                lifecycle,
                object : FileUpdateListener(permanent) {
                    override fun onCreate(file: LocalFile) {
                        fileUpdatedListener(file)
                    }

                    override fun onModify(file: LocalFile) {
                        fileUpdatedListener(file)
                    }

                    override fun onDelete(file: LocalFile) {
                        fileUpdatedListener(file)
                    }
                }
            )
        }

        startListening(permanent, fileUpdatedListener)
        startListening(cache, fileUpdatedListener)

        Timber.d("Reading target file and sending to flow... $targetFile")
        if (targetFile.exists()) {
            targetFile.inputStream().use { it.readBytes() }.let { send(it) }
        }

        // Block flow until lifecycle is destroyed
        while (lifecycle.currentState != Lifecycle.State.DESTROYED) { delay(1) }
        Timber.d("Lifecycle destroyed.")

        stopListening(permanent, fileUpdatedListener)
        stopListening(cache, fileUpdatedListener)
    }

    /**
     * Downloads the metadata from the server, and if it has been updated, download the new file.
     */
    @WorkerThread
    suspend fun update(width: Int? = null, progress: (suspend (current: Long, max: Long) -> Unit)? = null) {
        val local = localMeta()
        val remote = remoteMeta()

        if (local == null || local.hash != remote.hash) {
            // If there's no local copy, or hashes do not match, download the file again
            val url = URLBuilder(remote.download)
                .apply {
                    // Always use the protocol indicated by BuildConfig
                    set(scheme = BuildConfig.PROTOCOL)

                    width?.let { parameters["width"] = it.toString() }
                }
                .build()
            Timber.d("Downloading $url...")
            ktorHttpClient.get(url) {
                onDownload { bytesSentTotal, contentLength ->
                    progress?.invoke(bytesSentTotal, contentLength)
                }
            }.let { response ->
                val status = response.status.value
                if (status !in 200..299) {
                    Timber.d(
                        "Server result was not successful. Error (%d): %s",
                        status,
                        response.bodyAsText()
                    )
                    throw IllegalStateException("Server failed to deliver a requested file. Status=$status. URL=$url.")
                }

                val channel = response.bodyAsChannel()
                targetFile.write(channel, remote)

                Timber.d("Downloaded image successfully written. Calling listeners...")
                fileUpdatedListeners[targetFile]?.forEach { it(targetFile) }
            }
        }
    }
}
