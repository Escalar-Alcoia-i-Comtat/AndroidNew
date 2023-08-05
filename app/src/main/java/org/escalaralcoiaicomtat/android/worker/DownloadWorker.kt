package org.escalaralcoiaicomtat.android.worker

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.RemoteFileInfo
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.files.LocalFile
import org.escalaralcoiaicomtat.android.utils.await
import org.escalaralcoiaicomtat.android.utils.json
import timber.log.Timber

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        const val DATA_INFO = "file-info"

        const val PROGRESS_CURRENT = "current"
        const val PROGRESS_MAXIMUM = "maximum"

        const val RESULT_ERROR = "error"
        const val RESULT_ERROR_DATA = "data"

        const val ERROR_MISSING_DATA = "missing-data"
        const val ERROR_SERVER = "server"

        const val NOTIFICATION_ID = 1

        suspend fun download(
            context: Context,
            remoteFileInfo: RemoteFileInfo,
            imageWidth: Int?,
            progress: suspend (current: Int, max: Int) -> Unit
        ): LocalFile {
            val workManager = WorkManager.getInstance(context)

            val uuid = remoteFileInfo.uuid
            val url = EndpointUtils.getDownload(uuid, imageWidth)

            Timber.d("Getting all work infos for $url...")

            // Get all works once, even the completed ones
            val workInfo: WorkInfo? = workManager.getWorkInfoById(uuid).await()
            val finished = workInfo?.state?.isFinished

            Timber.d("Other request running for $uuid? finished=$finished")

            // Check if all of them are finished
            if (finished != false) {
                Timber.d("Checking if local file already exist...")

                // If they are all finished, check if file already exists
                val file = remoteFileInfo.permanentOrCache(context)

                if (file.exists()) {
                    // Check if hashes match
                    val storedFileInfo = file.readMeta()
                    if (storedFileInfo.hash == remoteFileInfo.hash) {
                        Timber.d("Local file already exists, reading bytes...")
                        return file
                    }
                    Timber.d("Local file already exists, but hashes do not match. Removing and scheduling download...")
                    // If hashes do not match, remove stored files
                    file.delete()
                } else {
                    Timber.d("Local file doesn't exist.")
                }

                // If the file is not downloaded, schedule a new one
                val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setId(uuid)
                    .addTag(url)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setInputData(
                        workDataOf(DATA_INFO to remoteFileInfo.toJson().toString())
                    )
                    .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                    .build()
                Timber.d("Scheduling download for $url...")
                val scheduleResult = workManager
                    .enqueueUniqueWork(uuid.toString(), ExistingWorkPolicy.REPLACE, request)
                    .result
                    .await()
                Timber.d("Download scheduled. Result: $scheduleResult")
            }

            Timber.d("Getting work info (UUID=$uuid).")
            val result = workManager.getWorkInfosForUniqueWorkLiveData(uuid.toString())
                .await { list ->
                    // Only consider the last scheduled request
                    val info = list.last()
                    val current = info.progress.getInt(PROGRESS_CURRENT, -1)
                    val maximum = info.progress.getInt(PROGRESS_MAXIMUM, -1)
                    if (current >= 0 && maximum >= 0) {
                        Timber.d(
                            "Work updated (%d / %d). Finished: %s",
                            current,
                            maximum,
                            info.state.isFinished.toString()
                        )

                        progress(current, maximum)
                    } else {
                        Timber.d("Work updated (count=${list.size}). Finished: ${info.state.isFinished}")
                    }

                    info.state.isFinished
                }
                .last()
                .also { Timber.d("Download finished.") }

            val error = result.outputData.getString(RESULT_ERROR)
            if (error != null) {
                val errorData = result.outputData.getString(RESULT_ERROR_DATA)
                throw RuntimeException(
                    "Download failed with an error: $error.\n Data: $errorData"
                )
            }

            return remoteFileInfo.cache(context)
        }
    }

    private val notificationManager: NotificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(null)

    override suspend fun doWork(): Result {
        Timber.d("Started worker.")

        setProgress(null)

        val info = inputData.getString(DATA_INFO)
            ?.json
            ?.let { RemoteFileInfo.fromJson(it) }
            ?: run {
                Timber.w("Could not start download. DATA_INFO extra not given.")
                return Result.failure(workDataOf(RESULT_ERROR to ERROR_MISSING_DATA))
            }

        val url = info.download
            // Allow only secure urls
            .replace("http:", "https:")

        val targetFile = info.cache(applicationContext)

        var lastStep = 0

        Timber.d("Downloading $url...")
        ktorHttpClient.get(url) {
            onDownload { bytesSentTotal, contentLength ->
                // Send updates for 1/20th of the complete data
                val piece = contentLength / 20
                if (lastStep * piece < bytesSentTotal) {
                    setProgress(bytesSentTotal.toInt() to contentLength.toInt())
                    lastStep++
                }
            }
        }.let { response ->
            val status = response.status.value
            if (status !in 200..299) {
                Timber.d(
                    "Server result was not successful. Error (%d): %s",
                    status,
                    response.bodyAsText()
                )
                return Result.failure(
                    workDataOf(
                        RESULT_ERROR to ERROR_SERVER,
                        RESULT_ERROR_DATA to response.bodyAsText()
                    )
                )
            }

            val channel = response.bodyAsChannel()
            targetFile.write(channel, info)

            Timber.d("Finished download for $url")
        }

        return Result.success()
    }

    private suspend fun setProgress(progress: Pair<Int, Int>?) {
        setProgress(
            workDataOf(
                PROGRESS_CURRENT to progress?.first,
                PROGRESS_MAXIMUM to progress?.second
            )
        )
        setForeground(
            createForegroundInfo(progress)
        )
    }

    /**
     * Provides foreground information for foreground services.
     */
    private fun createForegroundInfo(progress: Pair<Int, Int>?): ForegroundInfo {
        val channelId = applicationContext.getString(R.string.notification_channel_downloads_id)
        val title = applicationContext.getString(R.string.notification_downloading_title)
        val message = applicationContext.getString(R.string.notification_downloading_message)
        val cancel = applicationContext.getString(R.string.action_cancel)

        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setOngoing(true)
            .setProgress(progress?.first ?: 0, progress?.second ?: 0, progress == null)
            .addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        // Create the tasks group
        val groupId = applicationContext.getString(R.string.notification_group_tasks_id)
        val groupName = applicationContext.getString(R.string.notification_group_tasks_name)
        notificationManager.createNotificationChannelGroup(
            NotificationChannelGroup(groupId, groupName)
        )

        // Create the downloads channel
        val channelId = applicationContext.getString(R.string.notification_channel_downloads_id)
        val name = applicationContext.getString(R.string.notification_channel_downloads_name)
        val description = applicationContext.getString(R.string.notification_channel_downloads_description)

        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW).apply {
            this.description = description
            this.group = groupId
        }
        notificationManager.createNotificationChannel(channel)
    }
}
