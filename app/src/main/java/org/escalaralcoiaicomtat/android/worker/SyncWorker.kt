package org.escalaralcoiaicomtat.android.worker

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.firstOrNull
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.appendUpdate
import org.escalaralcoiaicomtat.android.network.bodyAsJson
import org.escalaralcoiaicomtat.android.network.get
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.utils.map
import org.escalaralcoiaicomtat.android.utils.serialize
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val SYNC_WORKER_TAG = "sync_worker"
        private const val ONE_TIME_REQUEST_TAG = "one_time"
        private const val PERIODIC_SYNC_TAG = "periodic_sync"

        private const val UNIQUE_WORK_NAME = "sync_worker"
        private const val UNIQUE_PERIODIC_WORK_NAME = "periodic_sync_worker"

        private const val NOTIFICATION_ID = 2

        suspend fun synchronize(context: Context): LiveData<WorkInfo> {
            val uuid = UUID.randomUUID()
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setId(uuid)
                .addTag(SYNC_WORKER_TAG)
                .addTag(ONE_TIME_REQUEST_TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            val workManager = WorkManager.getInstance(context)

            Timber.d("Enqueuing synchronization...")

            // Enqueue the request, and wait until it's on queue
            workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
                .result
                .await()

            Timber.d("Synchronization enqueued")

            // Get the work information for surveillance
            return workManager.getWorkInfoByIdLiveData(uuid)
        }

        suspend fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                4, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .addTag(SYNC_WORKER_TAG)
                .addTag(PERIODIC_SYNC_TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            val workManager = WorkManager.getInstance(context)

            Timber.d("Enqueuing periodic synchronization...")

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
                .result
                .await()

            Timber.d("Periodic synchronization enqueued")
        }

        fun getLive(context: Context) = WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(SYNC_WORKER_TAG)
    }

    private val notificationManager: NotificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val database: AppDatabase by lazy { AppDatabase.getInstance(applicationContext) }

    private val dao = database.dataDao()

    private val serverSectors = arrayListOf<Long>()
    private val serverZones = arrayListOf<Long>()
    private val serverAreas = arrayListOf<Long>()

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        setForeground(
            createForegroundInfo()
        )

        return try {
            getTree()

            Result.success()
        } catch (e: JSONException) {
            Timber.e("Got an invalid response from server.", e)

            Result.retry()
        }
    }

    private suspend fun synchronizeAreas(data: JSONObject) {
        data.getJSONArray("areas")
            .also { Timber.d("Got ${it.length()} areas. Serializing and inserting into database.") }
            .apply {
                serialize(Area).forEach { area ->
                    serverAreas.add(area.id)
                    try {
                        dao.insert(area)
                    } catch (_: SQLiteConstraintException) {
                        dao.update(area)
                    }
                }
            }
            .map { index ->
                val areaJson = getJSONObject(index)
                synchronizeZones(areaJson)
            }
    }

    private suspend fun synchronizeZones(data: JSONObject) {
        data.getJSONArray("zones")
            .also { Timber.d("Got ${it.length()} zones. Serializing and inserting into database.") }
            .apply {
                serialize(Zone).forEach { zone ->
                    serverZones.add(zone.id)
                    try {
                        dao.insert(zone)
                    } catch (_: SQLiteConstraintException) {
                        dao.update(zone)
                    }
                }
            }
            .map { index ->
                val zoneJson = getJSONObject(index)
                synchronizeSectors(zoneJson)
            }
    }

    private suspend fun synchronizeSectors(data: JSONObject) {
        data.getJSONArray("sectors")
            .also { Timber.d("Got ${it.length()} sectors. Serializing and inserting into database.") }
            .apply {
                serialize(Sector).forEach { serverSector ->
                    serverSectors.add(serverSector.id)
                    val localSector = dao.getSector(serverSector.id)

                    patchSector(localSector, serverSector)
                }
            }
            .map { index ->
                // val zoneJson = getJSONObject(index)
                // TODO - synchronize sectors
            }
    }

    private suspend fun patchSector(
        localSector: Sector?,
        serverSector: Sector
    ) {
        if (localSector == null) {
            // No local copy of the sector, insert it
            Timber.d("- Got new sector! ID: ${serverSector.id}. Inserting...")
            dao.insert(serverSector)
            return
        }
        if (localSector.timestamp <= serverSector.timestamp) {
            // No modification is needed, up to date
            Timber.d("- Sector with ID: ${serverSector.id}. No update needed")
            return
        }
        if (localSector.timestamp < serverSector.timestamp) {
            // Local copy outdated, update with server
            Timber.d("- Sector updated! ID: ${serverSector.id}. Updating local copy...")
            dao.update(serverSector)
            return
        }

        // Server copy outdated, send patch

        val apiKey = Preferences.getApiKey(applicationContext).firstOrNull()
        if (apiKey == null) {
            Timber.w("Editing not unlocked, local modifications for sector ${localSector.id} will be lost.")
            return
        }

        Timber.d("- Sector updated locally. ID: ${serverSector.id}. Patching server...")

        ktorHttpClient.submitFormWithBinaryData(
            EndpointUtils.getUrl("sector/${localSector.id}"),
            formData = formData {
                appendUpdate("timestamp", localSector.timestamp, serverSector.timestamp)
                appendUpdate("displayName", localSector.displayName, serverSector.displayName)
                appendUpdate("point", localSector.point, serverSector.point)
                appendUpdate("kidsApt", localSector.kidsApt, serverSector.kidsApt)
                appendUpdate("sunTime", localSector.sunTime, serverSector.sunTime)
                appendUpdate("walkingTime", localSector.walkingTime, serverSector.walkingTime)
                appendUpdate("weight", localSector.weight, serverSector.weight)
                appendUpdate("zone", localSector.zoneId, serverSector.zoneId)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }.apply {
            if (status == HttpStatusCode.NoContent || status == HttpStatusCode.OK) {
                // Update successful
                Timber.d("Patched server with data from sector ${localSector.id}")
            } else {
                Timber.e("Could not update server with data from sector ${localSector.id}")
                throw RequestException(status, bodyAsJson())
            }
        }
    }

    /**
     * Fetches a list of [T] to get all the entries of the database for the selected type. This list
     * is iterated, and the [DataEntity.id] of each element is searched in [idList]. If it isn't
     * present in the list, it's requested to be deleted with [deleteMethod].
     *
     * @return The amount of elements removed.
     */
    private suspend fun <T : DataEntity> synchronizeDeletions(
        idList: List<Long>,
        daoFetch: suspend () -> List<T>,
        deleteMethod: suspend (T) -> Unit
    ) = daoFetch().count {
        if (!idList.contains(it.id)) {
            deleteMethod(it)
            true
        } else {
            false
        }
    }

    private suspend fun getTree() {
        Timber.d("Getting data tree...")
        get(
            EndpointUtils.getUrl("tree")
        ) { data ->
            data?.let { synchronizeAreas(it) }
                ?: Timber.d("Tree request didn't have any area.")
        }

        // Synchronize deletions
        synchronizeDeletions(serverAreas, dao::getAllAreas) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted areas from server.") }
        synchronizeDeletions(serverZones, dao::getAllZones) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted zones from server.") }
    }

    /**
     * Provides foreground information for foreground services.
     */
    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = applicationContext.getString(R.string.notification_channel_sync_id)
        val title = applicationContext.getString(R.string.notification_sync_title)
        val message = applicationContext.getString(R.string.notification_sync_message)
        val cancel = applicationContext.getString(R.string.action_cancel)

        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= 34) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
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
        val channelId = applicationContext.getString(R.string.notification_channel_sync_id)
        val name = applicationContext.getString(R.string.notification_channel_sync_name)
        val description =
            applicationContext.getString(R.string.notification_channel_sync_description)

        val channel =
            NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW).apply {
                this.description = description
                this.group = groupId
            }
        notificationManager.createNotificationChannel(channel)
    }
}
