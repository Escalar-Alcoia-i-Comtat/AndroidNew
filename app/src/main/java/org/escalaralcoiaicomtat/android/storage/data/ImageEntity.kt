package org.escalaralcoiaicomtat.android.storage.data

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.exception.remote.RemoteFileNotFoundException
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.files.SynchronizedFile
import org.escalaralcoiaicomtat.android.utils.await
import org.escalaralcoiaicomtat.android.utils.toast
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber

abstract class ImageEntity : DataEntity() {
    abstract val image: String

    val imageUUID: UUID by lazy { UUID.fromString(image.substringBeforeLast('.')) }

    private fun imageFile(
        context: Context,
        isScaled: Boolean = true
    ) = SynchronizedFile.create(
        context,
        imageUUID,
        isScaled
    )

    fun readImageFile(
        context: Context,
        lifecycle: Lifecycle,
        isScaled: Boolean = true
    ): Flow<ByteArray?> {
        val imageFile = imageFile(context, isScaled)

        return imageFile.read(lifecycle)
    }

    @Composable
    fun rememberImageFile(isScaled: Boolean = true): LiveData<ByteArray?> {
        val context = LocalContext.current

        val imageFile = remember { SynchronizedFile.create(context, imageUUID, isScaled) }

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
            val imageFile = imageFile(context, width != null || height != null)
            imageFile.update(width, height, progress)
        } catch (_: RemoteFileNotFoundException) {
            // Image has been removed from server, should delete the items' data and sync again
            Timber.w("Image for ${this::class.simpleName}#$id could not be found in server. Voiding data and syncing again.")

            withContext(Dispatchers.Main) {
                context.toast(R.string.toast_data_corrupt)
            }

            AppDatabase.getInstance(context)
                .dataDao()
                .deleteByImageUUID(image)

            SyncWorker.synchronize(context, force = true)
                .await { it.state.isFinished }
        } catch (e: IllegalStateException) {
            // Server is not available
            // TODO - show error
        }
    }
}
