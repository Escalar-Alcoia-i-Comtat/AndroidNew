package org.escalaralcoiaicomtat.android.storage.files

import android.content.Context
import android.os.FileObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.utils.FileUtils.dirSize
import org.escalaralcoiaicomtat.android.utils.json
import timber.log.Timber
import java.io.File
import java.util.UUID

class FilesCrate private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: FilesCrate? = null

        fun getInstance(context: Context): FilesCrate = instance ?: synchronized(this) {
            instance ?: FilesCrate(context).also { instance = it }
        }

        @Composable
        fun rememberInstance(): FilesCrate {
            val context = LocalContext.current
            return remember { getInstance(context) }
        }
    }

    private val permanentDir = File(context.filesDir, "permanent")
    private val cachesDir = context.cacheDir

    init {
        if (!permanentDir.exists()) {
            if (permanentDir.mkdirs()) {
                Timber.i("Created permanent directory.")
            } else {
                Timber.e("Could not create permanent directory.")
            }
        }

        if (!cachesDir.exists()) {
            if (cachesDir.mkdirs()) {
                Timber.i("Created caches directory.")
            } else {
                Timber.e("Could not create caches directory.")
            }
        }
    }

    /**
     * Provides File access to a file with the given name in the cache.
     */
    fun cache(uuid: UUID) = LocalFile(cachesDir, uuid)

    /**
     * Provides File access to a file with the given name in the permanent directory.
     */
    fun permanent(uuid: UUID) = LocalFile(permanentDir, uuid)

    fun insert(info: RemoteFileInfo) {
        val metadataFile = File(permanentDir, info.filename + ".meta")
        metadataFile.writeText(info.toJson().toString())
    }

    fun getMetadata(uuid: String): RemoteFileInfo? {
        val metadataFile = File(permanentDir, "$uuid.meta")
        Timber.i("Metadata file: $metadataFile")
        if (!metadataFile.exists()) return null
        return metadataFile
            .readText()
            .json
            .let { RemoteFileInfo.fromJson(it) }
    }

    private val observers = mutableStateListOf<FileObserver>()

    @Composable
    fun existsLive(uuid: UUID): LiveData<Boolean> = existsLive(permanent(uuid))

    @Composable
    fun existsLiveCache(uuid: UUID): LiveData<Boolean> = existsLive(cache(uuid))

    @Composable
    fun existsLive(
        path: LocalFile
    ): LiveData<Boolean> {
        val liveData = MutableLiveData(path.exists())

        DisposableEffect(path) {
            val observer = object : FileUpdateListener(path) {
                override fun onCreate(file: LocalFile) { liveData.postValue(file.exists()) }
                override fun onModify(file: LocalFile) { liveData.postValue(file.exists()) }
                override fun onDelete(file: LocalFile) { liveData.postValue(file.exists()) }
                override fun onMovedFrom(file: LocalFile) { liveData.postValue(file.exists()) }
                override fun onMovedTo(file: LocalFile) { liveData.postValue(file.exists()) }
            }

            val fileObserver = path.observer { event, _ ->
                Timber.d("Got update ($event) for path: $path")
                if (event == FileObserver.MOVE_SELF) return@observer

                when (event) {
                    FileObserver.ACCESS -> observer.onAccess(path)
                    FileObserver.ATTRIB -> observer.onAttrib(path)
                    FileObserver.CLOSE_NOWRITE -> observer.onCloseNoWrite(path)
                    FileObserver.CLOSE_WRITE -> observer.onCloseWrite(path)
                    FileObserver.CREATE -> observer.onCreate(path)
                    FileObserver.DELETE -> observer.onDelete(path)
                    FileObserver.DELETE_SELF -> observer.onDeleteSelf(path)
                    FileObserver.MODIFY -> observer.onModify(path)
                    FileObserver.MOVED_FROM -> observer.onMovedFrom(path)
                    FileObserver.MOVED_TO -> observer.onMovedTo(path)
                    FileObserver.OPEN -> observer.onOpen(path)
                }
            }
            observers.add(fileObserver)
            fileObserver.startWatching()

            Timber.d("Started listening for updates on $path (${path.exists()})")

            onDispose {
                observers.remove(fileObserver)
                fileObserver.stopWatching()
            }
        }

        return liveData
    }

    @Composable
    fun cacheSize(): LiveData<Long> = MutableLiveData<Long>().apply {
        DisposableEffect(cachesDir) {
            @Suppress("Deprecation")
            val file = LocalFile(cachesDir, cachesDir)

            val observer = object : FileUpdateListener(file) {
                override fun onAny(file: LocalFile) {
                    postValue(cachesDir.dirSize())
                }
            }

            val fileObserver = file.observer(observer)
            observers.add(fileObserver)
            fileObserver.startWatching()

            Timber.d("Started listening for updates on $file")

            onDispose {
                observers.remove(fileObserver)
                fileObserver.stopWatching()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val size = cachesDir.dirSize()
            postValue(size)
        }
    }

    fun cacheClear() = CoroutineScope(Dispatchers.Main).launch {
        withContext(Dispatchers.IO) {
            cachesDir.deleteRecursively()
        }
    }
}
