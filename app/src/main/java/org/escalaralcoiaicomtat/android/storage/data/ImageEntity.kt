package org.escalaralcoiaicomtat.android.storage.data

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import org.escalaralcoiaicomtat.android.exception.remote.RemoteFileNotFoundException
import org.escalaralcoiaicomtat.android.storage.files.SynchronizedFile
import org.escalaralcoiaicomtat.android.utils.await
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber
import java.util.UUID

abstract class ImageEntity : DataEntity() {
    abstract val image: String

    val imageUUID: UUID by lazy { UUID.fromString(image.substringBeforeLast('.')) }

    private fun imageFile(context: Context) = SynchronizedFile.create(context, imageUUID)

    fun readImageFile(context: Context, lifecycle: Lifecycle): Flow<ByteArray?> {
        val imageFile = imageFile(context)

        return imageFile.read(lifecycle)
    }

    @Composable
    fun rememberImageFile(): LiveData<ByteArray?> {
        val context = LocalContext.current

        val imageFile = remember { SynchronizedFile.create(context, imageUUID) }

        return imageFile.rememberImageData()
    }

    @WorkerThread
    suspend fun updateImageIfNeeded(
        context: Context,
        width: Int? = null,
        height: Int? = null,
        progress: (suspend (current: Long, max: Long) -> Unit)? = null
    ) {
        Timber.d("Checking if image file needs to be updated...")
        try {
            val imageFile = imageFile(context)
            imageFile.update(width, height, progress)
        } catch (_: RemoteFileNotFoundException) {
            // Image has been removed from server, should fetch data again
            SyncWorker.synchronize(context).await { it.state.isFinished }
        } catch (e: IllegalStateException) {
            // Server is not available
            // TODO - show error
            throw e
        }
    }
}
