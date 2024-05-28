package org.escalaralcoiaicomtat.android.storage.data

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.escalaralcoiaicomtat.android.exception.remote.RemoteFileNotFoundException
import org.escalaralcoiaicomtat.android.storage.files.LocalFile
import org.escalaralcoiaicomtat.android.storage.files.SynchronizedFile
import timber.log.Timber

abstract class GpxEntity : ImageEntity() {
    abstract val gpx: String?

    private val gpxUUID: UUID? by lazy { gpx?.substringBeforeLast('.')?.let(UUID::fromString) }

    private fun gpxFile(context: Context) = gpxUUID?.let {
        SynchronizedFile.create(context, it, isScaled = false)
    }

    fun gpxFileIntent(context: Context): Intent? {
        val externalFilesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ) ?: return null
        val appExternalFilesDir = File(externalFilesDir, "EscalarAlcoiaIComtat")
        val gpxFile = gpxFile(context) ?: return null
        val gpxLocalFile = gpxFile.permanent.takeIf { it.exists() } ?: gpxFile.cache
        val externalGpxFile = LocalFile(appExternalFilesDir, gpxFile.uuid, ".gpx")

        if (!externalGpxFile.exists()) gpxLocalFile.copyTo(externalGpxFile)

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(externalGpxFile.getUri(context), "text/xml")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun readGpxFile(
        context: Context,
        lifecycle: Lifecycle
    ): Flow<ByteArray?> {
        val gpxFile = gpxFile(context)

        return gpxFile?.read(lifecycle) ?: emptyFlow()
    }

    @Composable
    fun rememberGpxFile(): StateFlow<ByteArray?> {
        val context = LocalContext.current

        val gpxFile = remember { gpxFile(context) }

        return gpxFile?.rememberDataFlow() ?: MutableStateFlow(null)
    }

    @WorkerThread
    suspend fun updateGpxIfNeeded(
        context: Context,
        progress: (suspend (current: Long, max: Long) -> Unit)? = null
    ) {
        Timber.d("Checking if image file needs to be updated...")
        val gpxFile = gpxFile(context)
        try {
            gpxFile?.update(progress = progress)
        } catch (_: RemoteFileNotFoundException) {
            // GPX has been removed from server, delete local file
            Timber.w("GPX for ${this::class.simpleName}#$id could not be found in server. Removing local file...")
            gpxFile?.cache?.delete()
            gpxFile?.permanent?.delete()
        } catch (e: IllegalStateException) {
            // Server is not available
            // TODO - show error
            Timber.e(e, "Could not update GPX file for ${this::class.simpleName}#$id")
        }
    }
}
