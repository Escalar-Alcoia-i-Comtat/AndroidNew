package org.escalaralcoiaicomtat.android.storage.files

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.set
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        @Volatile
        private var fileUpdatedListeners: Map<String, List<(LocalFile) -> Unit>> = emptyMap()

        fun create(context: Context, uuid: UUID, isScaled: Boolean = true): SynchronizedFile {
            val crate = FilesCrate.getInstance(context)

            return SynchronizedFile(
                uuid,
                crate.cache(uuid, if (isScaled) null else ".fullsize"),
                crate.permanent(uuid, if (isScaled) null else ".fullsize")
            )
        }
    }

    /**
     * Will be [permanent] if it exists, or [cache] otherwise.
     */
    private val targetFile get() = if (permanent.exists()) permanent else cache

    @Synchronized
    private fun startListening(file: LocalFile, listener: (LocalFile) -> Unit) {
        val list = fileUpdatedListeners.getOrDefault(file.toString(), emptyList()).toMutableList()
        list.add(listener)
        val map = fileUpdatedListeners.toMutableMap()
        map[file.toString()] = list
        fileUpdatedListeners = map
        Timber.d("Started listening for $file")
    }

    @Synchronized
    private fun stopListening(file: LocalFile, listener: (LocalFile) -> Unit) {
        val list = fileUpdatedListeners.getOrDefault(file.toString(), emptyList()).toMutableList()
        list.remove(listener)
        val map = fileUpdatedListeners.toMutableMap()
        map[file.toString()] = list
        fileUpdatedListeners = map
        Timber.d("Stopped listening for $file")
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
     * @throws IllegalStateException If the server didn't respond with JSON
     */
    suspend fun remoteMeta(): RemoteFileInfo {
        try {
            val endpoint = EndpointUtils.getFile(uuid.toString())
            var result: RemoteFileInfo? = null
            get(endpoint) { data ->
                requireNotNull(data)

                val files = data.getJSONArray("files")
                val file = files.getJSONObject(0)
                result = RemoteFileInfo(
                    download = file.getString("download"),
                    filename = file.getString("filename"),
                    size = file.getLong("size"),
                    hash = file.getString("hash"),
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
        while (lifecycle.currentState != Lifecycle.State.DESTROYED) {
            delay(1)
        }
        Timber.d("Lifecycle destroyed.")

        stopListening(permanent, fileUpdatedListener)
        stopListening(cache, fileUpdatedListener)
    }

    @Composable
    @Deprecated("Use Flows", replaceWith = ReplaceWith("rememberData()"))
    fun rememberImageData(): LiveData<ByteArray?> =
        remember { MutableLiveData<ByteArray?>(null) }.apply {
            DisposableEffect(this) {
                val fileUpdatedListener: (LocalFile) -> Unit = { file ->
                    if (file.exists()) {
                        Timber.d("Got update to $file. Sending update...")
                        val bytes = file.inputStream().use { it.readBytes() }
                        runBlocking { postValue(bytes) }
                    } else {
                        Timber.w("Could not read $file. It doesn't exist.")
                        runBlocking { postValue(null) }
                    }
                }

                val observer = object : FileUpdateListener(permanent) {
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

                val permanentObserver = permanent.observer(observer)
                val cacheObserver = cache.observer(observer)

                permanentObserver.forEach { it.startWatching() }
                cacheObserver.forEach { it.startWatching() }

                startListening(permanent, fileUpdatedListener)
                startListening(cache, fileUpdatedListener)

                if (targetFile.exists()) {
                    Timber.d("Reading target file and sending to flow... $targetFile")
                    targetFile.inputStream().use { it.readBytes() }.let { postValue(it) }
                }

                onDispose {
                    permanentObserver.forEach { it.stopWatching() }
                    cacheObserver.forEach { it.stopWatching() }

                    stopListening(permanent, fileUpdatedListener)
                    stopListening(cache, fileUpdatedListener)
                }
            }
        }

    @Composable
    fun rememberDataFlow(): StateFlow<ByteArray?> =
        remember { MutableStateFlow<ByteArray?>(null) }.apply {
            DisposableEffect(this) {
                val fileUpdatedListener: (LocalFile) -> Unit = { file ->
                    if (file.exists()) {
                        Timber.d("Got update to $file. Sending update...")
                        val bytes = file.inputStream().use { it.readBytes() }
                        tryEmit(bytes)
                    } else {
                        Timber.w("Could not read $file. It doesn't exist.")
                        tryEmit(null)
                    }
                }

                val observer = object : FileUpdateListener(permanent) {
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

                val permanentObserver = permanent.observer(observer)
                val cacheObserver = cache.observer(observer)

                permanentObserver.forEach { it.startWatching() }
                cacheObserver.forEach { it.startWatching() }

                startListening(permanent, fileUpdatedListener)
                startListening(cache, fileUpdatedListener)

                if (targetFile.exists()) {
                    Timber.d("Reading target file and sending to flow... $targetFile")
                    targetFile.inputStream().use { it.readBytes() }.let { tryEmit(it) }
                }

                onDispose {
                    permanentObserver.forEach { it.stopWatching() }
                    cacheObserver.forEach { it.stopWatching() }

                    stopListening(permanent, fileUpdatedListener)
                    stopListening(cache, fileUpdatedListener)
                }
            }
        }

    /**
     * Downloads the metadata from the server, and if it has been updated, download the new file.
     *
     * @throws RemoteFileNotFoundException If there isn't any file with [uuid] on the server.
     * @throws RequestException If there's any other error on the request.
     * @throws JSONException If there's any error of format in the response.
     * @throws AssertionError If the response doesn't have any data.
     * @throws IllegalStateException If the server didn't respond with JSON
     */
    @WorkerThread
    suspend fun update(
        width: Int? = null,
        height: Int? = null,
        progress: (suspend (current: Long, max: Long) -> Unit)? = null
    ) {
        val local = localMeta()
        val remote = remoteMeta()

        if (local == null || local.hash != remote.hash) {
            // If there's no local copy, or hashes do not match, download the file again
            val url = URLBuilder(remote.download)
                .apply {
                    // Always use the protocol indicated by BuildConfig
                    set(scheme = BuildConfig.PROTOCOL)

                    width?.let { parameters["width"] = it.toString() }
                    height?.let { parameters["height"] = it.toString() }
                }
                .build()
            Timber.d("Downloading $url...")
            ktorHttpClient.get(url) {
                onDownload { bytesSentTotal, contentLength ->
                    contentLength ?: return@onDownload
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

                Timber.d("Downloaded image ($targetFile) successfully written. Calling listeners...")
                val listeners = fileUpdatedListeners[targetFile.toString()]
                if (listeners.isNullOrEmpty())
                    Timber.w("There are no listeners for $targetFile to call.")
                else
                    listeners.forEach { it(targetFile) }
            }
        }
    }
}
