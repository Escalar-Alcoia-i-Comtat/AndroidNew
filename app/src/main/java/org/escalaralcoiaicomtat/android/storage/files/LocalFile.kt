package org.escalaralcoiaicomtat.android.storage.files

import android.os.Build
import android.os.FileObserver
import coil.request.ImageRequest
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.utils.json
import java.io.File
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
    constructor(parent: File, uuid: UUID): this(
        File(parent, "$uuid"),
        File(parent, "$uuid.meta")
    )

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
    fun observer(listener: (event: Int, path: File?) -> Unit): FileObserver =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(listOf(file, meta)) {
                override fun onEvent(event: Int, path: String?) {
                    listener(event, path?.let { File(it) })
                }
            }
        } else {
            object : FileObserver(file.path) {
                override fun onEvent(event: Int, path: String?) {
                    listener(event, path?.let { File(it) })
                }
            }
        }

    fun observer(listener: FileUpdateListener) = observer { event, _ ->
        if (event != FileObserver.MOVE_SELF) {
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
                FileObserver.MOVED_FROM -> listener.onMovedFrom(this)
                FileObserver.MOVED_TO -> listener.onMovedTo(this)
                FileObserver.OPEN -> listener.onOpen(this)
            }
        }
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
     * Writes the given data into the target and meta files.
     *
     * @param dataBytes The data to write into the data file.
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

        dataBytes.copyAndClose(file.writeChannel())
        meta.writeText(metaData.toJson().toString(), charset)
    }
}
