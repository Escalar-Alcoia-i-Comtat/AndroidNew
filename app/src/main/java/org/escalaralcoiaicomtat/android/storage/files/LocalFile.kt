package org.escalaralcoiaicomtat.android.storage.files

import android.os.Build
import android.os.FileObserver
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.request.ImageRequest
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.utils.json
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.Charset
import java.util.UUID

/**
 * Provides a way of handling files that require additional metadata ([RemoteFileInfo]).
 */
class LocalFile
@Deprecated(
    "Should not be used, please use the builder from parent and UUID.",
    replaceWith = ReplaceWith("LocalFile(parent, uuid)")
)
constructor(private val file: File, private val meta: File) {
    companion object {
        @Volatile
        private var states: Map<String, List<MutableState<Boolean>>> = emptyMap()

        /**
         * Uses [ImageRequest.Builder.data] to set the [file] as data for the builder.
         */
        fun ImageRequest.Builder.file(file: LocalFile) = data(file.file)
    }

    /**
     * @param parent The parent directory where the data and meta files will be stored.
     * @param uuid The UUID of the file. This is usually the file's name.
     */
    @Suppress("Deprecation")
    constructor(parent: File, uuid: UUID) : this(
        File(parent, "$uuid"),
        File(parent, "$uuid.meta")
    )

    /**
     * Tries to get an [FileInputStream] for reading the file's contents.
     *
     * @throws FileNotFoundException If the file doesn't exist.
     */
    fun inputStream(): FileInputStream = file.inputStream()

    /**
     * Returns true if both the data and meta file exists.
     */
    fun exists() = file.exists() && meta.exists()

    /**
     * Deletes both the data and meta files.
     *
     * @return Whether the files were deleted successfully.
     */
    fun delete() = file.delete() && meta.delete()

    /**
     * Copies this file into the given [target].
     *
     * @param target The new [LocalFile] to copy this into.
     * @param overwrite If `true`, and [target] exists, it will be deleted before copying.
     *                  If `false`, and [target] exists, this function will do nothing.
     *
     * @return [target]
     *
     * @throws IllegalArgumentException If this file doesn't exist.
     * @throws IOException If there was an error while copying the file.
     */
    fun copyTo(target: LocalFile, overwrite: Boolean = false): LocalFile {
        if (!exists()) throw IllegalArgumentException("this file doesn't exist.")

        if (!overwrite && target.exists()) return target
        if (target.exists()) target.delete()

        file.copyTo(target.file, overwrite)
        meta.copyTo(target.meta, overwrite)

        if (!target.file.exists() || !target.meta.exists())
            throw IOException("The file was not copied successfully.")

        synchronized(states) {
            states[file.path]?.forEach { it.value = file.exists() }
            states[target.file.path]?.forEach { it.value = target.file.exists() }
        }

        return target
    }

    /**
     * Provides a [FileObserver] for this file. On API 28 and lower, only the data file will be
     * observed. This method only provides the observer, but doesn't start listening it, you must
     * call [FileObserver.startWatching] for that.
     *
     * @param listener Will get called when a file event is received into [FileObserver].
     */
    @Suppress("DEPRECATION")
    private fun observer(listener: (event: Int, path: File?) -> Unit): Set<FileObserver> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setOf(
                object : FileObserver(listOf(file, meta)) {
                    override fun onEvent(event: Int, path: String?) {
                        listener(event, path?.let { File(it) })
                    }
                }
            )
        } else {
            setOf(
                object : FileObserver(file.path) {
                    override fun onEvent(event: Int, path: String?) {
                        listener(event, path?.let { File(it) })
                    }
                },
                object : FileObserver(meta.path) {
                    override fun onEvent(event: Int, path: String?) {
                        listener(event, path?.let { File(it) })
                    }
                }
            )
        }

    fun observer(listener: FileUpdateListener) = observer { event, _ ->
        listener.onAny(this)
        when (event) {
            FileObserver.ACCESS -> listener.onAccess(this)
            FileObserver.ATTRIB -> listener.onAttrib(this)
            FileObserver.CLOSE_NOWRITE -> listener.onCloseNoWrite(this)
            FileObserver.CLOSE_WRITE -> listener.onCloseWrite(this)
            FileObserver.CREATE -> listener.onCreate(this)
            FileObserver.DELETE -> listener.onDelete(this)
            FileObserver.DELETE_SELF -> listener.onDeleteSelf(this)
            FileObserver.MODIFY -> listener.onModify(this)
            FileObserver.MOVE_SELF -> listener.onMoveSelf(this)
            FileObserver.MOVED_FROM -> listener.onMovedFrom(this)
            FileObserver.MOVED_TO -> listener.onMovedTo(this)
            FileObserver.OPEN -> listener.onOpen(this)
        }
    }

    /**
     * Starts watching for changes on the current file, following the state of the given [lifecycle]
     *
     * @param lifecycle The lifecycle that's holding the listener.
     * @param listener Will get called with updates to the file.
     */
    @MainThread
    fun watch(lifecycle: Lifecycle, listener: FileUpdateListener) {
        val fileObserver = observer(listener)

        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                fileObserver.forEach { it.startWatching() }
            }

            override fun onPause(owner: LifecycleOwner) {
                fileObserver.forEach { it.stopWatching() }
            }
        }
        lifecycle.addObserver(lifecycleObserver)

        fileObserver.forEach { it.startWatching() }
    }

    /**
     * Converts this into a String. Returns the path of the data file.
     */
    override fun toString(): String = file.path

    /**
     * Reads the contents of the meta file.
     *
     * @param charset The charset to use for reading the file.
     *
     * @throws IllegalStateException If the meta file doesn't exist.
     */
    suspend fun readMeta(
        charset: Charset = Charsets.UTF_8
    ): RemoteFileInfo = withContext(Dispatchers.IO) {
        if (!exists()) throw IllegalStateException("Meta file doesn't exist.")
        val text = meta.readText(charset)
        val json = text.json
        RemoteFileInfo.fromJson(json)
    }

    /**
     * Writes the given meta info to the meta file.
     *
     * @param remoteFileInfo The info to write. Will be converted into a JSON string to write.
     * @param charset The charset to use while writing.
     *
     * @throws IOException If there's any problem in the writing of the file.
     */
    suspend fun writeMeta(
        remoteFileInfo: RemoteFileInfo,
        charset: Charset = Charsets.UTF_8
    ) = withContext(Dispatchers.IO) {
        if (meta.exists() && !meta.delete()) {
            throw IOException("Could not delete existing meta file.")
        }

        meta.writeText(remoteFileInfo.toJson().toString(), charset)

        if (!meta.exists()) {
            throw IOException("An unknown error occurred while writing the meta file: meta doesn't exist.")
        }
    }

    /**
     * Writes the given data into the target and meta files.
     *
     * @param dataBytes The data to write into the data file. The channel is closed automatically.
     * @param metaData The metadata to write into the meta file.
     * @param overwrite If `true`, and the file exists, it will be deleted before copying.
     *                  If `false`, and the file exists, this function will do nothing.
     * @param charset The charset to use for writing the file.
     */
    suspend fun write(
        dataBytes: ByteReadChannel,
        metaData: RemoteFileInfo,
        overwrite: Boolean = false,
        charset: Charset = Charsets.UTF_8
    ) = withContext(Dispatchers.IO) {
        if (!overwrite && exists()) return@withContext
        if (exists()) delete()

        file.createNewFile()
        dataBytes.copyAndClose(file.writeChannel())
        meta.writeText(metaData.toJson().toString(), charset)
    }

    suspend fun write(
        byteArray: ByteArray,
        metaData: RemoteFileInfo,
        overwrite: Boolean = false,
        charset: Charset = Charsets.UTF_8
    ) = withContext(Dispatchers.IO) {
        if (!overwrite && exists()) return@withContext
        if (exists()) delete()

        file.outputStream().write(byteArray)
        meta.writeText(metaData.toJson().toString(), charset)
    }

    @Composable
    fun existsLive(): State<Boolean> = remember { mutableStateOf(exists()) }.apply {
        DisposableEffect(this) {
            val fileObserver = observer(
                object : FileUpdateListener(this@LocalFile) {
                    override fun onAny(file: LocalFile) {
                        Timber.d("File updated (${file.exists()}): $file")
                        value = file.exists()
                    }
                }
            )

            Timber.d("Started watching for $file")
            fileObserver.forEach(FileObserver::startWatching)

            synchronized(states) {
                val map = states.toMutableMap()
                val list = map[file.path]?.toMutableList() ?: arrayListOf()
                list.add(this@apply)
                map[file.path] = list
                states = map
            }

            onDispose {
                Timber.d("Stopped watching for $file")
                fileObserver.forEach(FileObserver::stopWatching)

                synchronized(states) {
                    val map = states.toMutableMap()
                    val list = map[file.path]?.toMutableList() ?: arrayListOf()
                    list.remove(this@apply)
                    map[file.path] = list
                    states = map
                }
            }
        }
    }
}
