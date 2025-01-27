package org.escalaralcoiaicomtat.android.worker

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
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
import androidx.work.workDataOf
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.sentry.ITransaction
import io.sentry.Sentry
import io.sentry.SpanStatus
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
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
import org.escalaralcoiaicomtat.android.storage.dao.DataDao
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.propertiesWith
import org.escalaralcoiaicomtat.android.utils.await
import org.escalaralcoiaicomtat.android.utils.getLongOrNull
import org.escalaralcoiaicomtat.android.utils.map
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.escalaralcoiaicomtat.android.utils.serialize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val SYNC_WORKER_TAG = "sync_worker"
        private const val ONE_TIME_REQUEST_TAG = "one_time"
        private const val PERIODIC_SYNC_TAG = "periodic_sync"

        private const val UNIQUE_WORK_NAME = "sync_worker"
        private const val UNIQUE_PERIODIC_WORK_NAME = "periodic_sync_worker"

        private const val NOTIFICATION_ID = 2

        private const val PROGRESS_STEP = "step"
        private const val PROGRESS_PROGRESS = "progress"
        private const val PROGRESS_MAX = "max"

        private const val DATA_FORCE = "force"

        const val RESULT_STOP_REASON = "stop_reason"

        suspend fun synchronize(context: Context, force: Boolean = false): LiveData<WorkInfo?> {
            // TODO - allow running sync just for specific parts, eg just for blocks

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
                .setInputData(
                    workDataOf(
                        DATA_FORCE to force
                    )
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

        @Deprecated(
            message = "Use Flows instead of LiveData",
            replaceWith = ReplaceWith("getFlow(context)")
        )
        fun getLive(context: Context) = WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(SYNC_WORKER_TAG)

        fun getFlow(context: Context) = WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(SYNC_WORKER_TAG)
    }

    private val notificationManager: NotificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val database: AppDatabase by lazy { AppDatabase.getInstance(applicationContext) }

    private val dao = database.dataDao()

    private val serverBlockings = arrayListOf<Long>()
    private val serverPaths = arrayListOf<Long>()
    private val serverSectors = arrayListOf<Long>()
    private val serverZones = arrayListOf<Long>()
    private val serverAreas = arrayListOf<Long>()

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo()

    override suspend fun doWork(): Result {
        Timber.i("Synchronization started.")

        // setForeground(createForegroundInfo())

        val transaction = Sentry.startTransaction("SyncWorker", "doWork()")
        transaction.setData("force", inputData.getBoolean(DATA_FORCE, false))

        return try {
            if (!shouldRunSynchronization(transaction)) {
                return Result.success(
                    workDataOf(RESULT_STOP_REASON to StopReason.ALREADY_UP_TO_DATE.name)
                )
            }

            getTree()

            // TODO - sync blocks

            // Get last update from server
            get(EndpointUtils.getUrl("last_update")) { data ->
                if (data == null) {
                    Timber.w("Last update from server didn't return any data.")
                } else {
                    val lastUpdate = data.getLongOrNull("last_update")?.let(Instant::ofEpochMilli)
                    Preferences.setLastUpdate(applicationContext, lastUpdate)
                }
            }

            Preferences.markAsSynchronized(applicationContext)
            Preferences.setLastSync(applicationContext, Instant.now())
            Preferences.setLastModification(applicationContext, null)

            transaction.status = SpanStatus.OK

            Result.success(
                workDataOf(RESULT_STOP_REASON to StopReason.SYNC_COMPLETED.name)
            )
        } catch (e: JSONException) {
            Timber.e(e, "Got an invalid response from server.")
            transaction.throwable = e
            transaction.status = SpanStatus.INTERNAL_ERROR

            Result.retry()
        } catch (e: IOException) {
            Timber.w(e, "Internet or server is not reachable. Sync failed.")
            transaction.throwable = e
            transaction.status = SpanStatus.INTERNAL_ERROR

            Result.failure(
                workDataOf(RESULT_STOP_REASON to StopReason.ERROR.name)
            )
        } catch (e: Exception) {
            Timber.e(e, "An error occurred while synchronizing.")
            transaction.throwable = e
            transaction.status = SpanStatus.UNKNOWN_ERROR

            Result.failure(
                workDataOf(RESULT_STOP_REASON to StopReason.ERROR.name)
            )
        } finally {
            transaction.finish()
        }
    }

    sealed class DataTypeInfo<R : BaseEntity>(
        val endpoint: String,
        val arrayKey: String,
        val daoGet: suspend DataDao.(Long) -> R?,
        val daoInsert: suspend DataDao.(R) -> Unit,
        val daoUpdate: suspend DataDao.(R) -> Unit
    ) {
        data object AreaInfo : DataTypeInfo<Area>(
            "area",
            "areas",
            DataDao::getArea,
            DataDao::insert,
            DataDao::update
        )

        data object ZoneInfo : DataTypeInfo<Zone>(
            "zone",
            "zones",
            DataDao::getZone,
            DataDao::insert,
            DataDao::update
        )

        data object SectorInfo : DataTypeInfo<Sector>(
            "sector",
            "sectors",
            DataDao::getSector,
            DataDao::insert,
            DataDao::update
        )

        data object PathInfo : DataTypeInfo<Path>(
            "path",
            "paths",
            DataDao::getPath,
            DataDao::insert,
            DataDao::update
        )

        data object BlockingInfo : DataTypeInfo<Blocking>(
            "block",
            String(),
            DataDao::getBlocking,
            DataDao::insert,
            DataDao::update
        )
    }

    private suspend inline fun <reified R : BaseEntity> synchronize(
        data: JSONObject,
        serializer: JsonSerializer<R>,
        serverCache: MutableList<Long>,
        info: DataTypeInfo<R>,
        step: Step,
        forEach: (JSONObject) -> Unit
    ) = with(info) {
        val array = data.getJSONArray(arrayKey)
            .also { Timber.d("Got ${it.length()} $arrayKey. Serializing and inserting into database.") }
            .apply {
                val list = serialize(serializer)
                for ((i, server) in list.withIndex()) {
                    serverCache.add(server.id)
                    val local = dao.daoGet(server.id)

                    patch(local, server, info)
                    setProgress(step, i, list.size)
                }
            }

        array.map { index ->
            val json = getJSONObject(index)
            forEach(json)
        }
    }

    private suspend inline fun <reified R : BaseEntity> patch(
        local: R?,
        server: R,
        info: DataTypeInfo<R>
    ) = with(info) {
        if (local == null) {
            // No local copy of the element, insert it
            Timber.d("- Got new $endpoint! ID: ${server.id}. Inserting...")
            dao.daoInsert(server)
            return@with
        }
        if (local.timestamp <= server.timestamp) {
            // No modification is needed, up to date
            Timber.d("- $endpoint with ID: ${server.id}. No update needed")
            return
        }
        if (local.timestamp < server.timestamp) {
            // Local copy outdated, update with server
            Timber.d("- $endpoint updated! ID: ${server.id}. Updating local copy...")
            dao.daoUpdate(server)
            return
        }

        // Server copy outdated, send patch

        val apiKey = Preferences.getApiKey(applicationContext).firstOrNull()
        if (apiKey == null) {
            Timber.w("Editing not unlocked, local modifications for $endpoint ${local.id} will be lost.")
            return
        }

        Timber.d("- $endpoint updated locally. ID: ${server.id}. Patching server...")

        ktorHttpClient.submitFormWithBinaryData(
            EndpointUtils.getUrl("$endpoint/${local.id}"),
            formData = formData {
                val pairs = local propertiesWith server
                for ((name, value1, value2) in pairs) {
                    appendUpdate(name, value1, value2)
                }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }.apply {
            if (status == HttpStatusCode.NoContent || status == HttpStatusCode.OK) {
                // Update successful
                Timber.d("Patched server with data from $endpoint ${local.id}")
            } else {
                Timber.e("Could not update server with data from $endpoint ${local.id}")
                throw RequestException(status, bodyAsJson())
            }
        }
    }

    /**
     * Fetches a list of [T] to get all the entries of the database for the selected type. This list
     * is iterated, and the [BaseEntity.id] of each element is searched in [idList]. If it isn't
     * present in the list, it's requested to be deleted with [deleteMethod].
     *
     * @return The amount of elements removed.
     */
    private suspend fun <T : BaseEntity> synchronizeDeletions(
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

    private suspend fun performDeletions(
        apiKey: String,
        type: String,
        serverCache: MutableList<Long>?
    ) {
        dao.pendingDeletions(type).forEach { deletion ->
            ktorHttpClient.delete(EndpointUtils.getUrl(deletion.endpoint)) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }.apply {
                when (status) {
                    HttpStatusCode.OK -> {
                        // Deletion successful
                        Timber.d("Deleted data from server successfully: ${deletion.type}:${deletion.deleteId}")
                        serverCache?.remove(deletion.deleteId)
                    }

                    HttpStatusCode.Gone -> {
                        // Deletion successful
                        Timber.d("Data from server was already deleted: ${deletion.type}:${deletion.deleteId}")
                        serverCache?.remove(deletion.deleteId)
                    }

                    else -> {
                        Timber.e("Could not delete data from server: ${deletion.type}:${deletion.deleteId}")
                        throw RequestException(status, bodyAsJson())
                    }
                }
            }

            dao.clearDeletion(deletion)
        }
    }

    private suspend fun getTree() {
        Timber.d("Getting data tree...")
        setProgress(Step.GET_TREE)
        get(
            EndpointUtils.getUrl("tree")
        ) { data ->
            data?.let { aJson ->
                Timber.d("Data Tree ready.")
                synchronize(
                    aJson,
                    Area,
                    serverAreas,
                    DataTypeInfo.AreaInfo,
                    Step.SYNC_AREAS
                ) { zJson ->
                    synchronize(
                        zJson,
                        Zone,
                        serverZones,
                        DataTypeInfo.ZoneInfo,
                        Step.SYNC_ZONES
                    ) { sJson ->
                        synchronize(
                            sJson,
                            Sector,
                            serverSectors,
                            DataTypeInfo.SectorInfo,
                            Step.SYNC_SECTORS
                        ) { pJson ->
                            synchronize(
                                pJson,
                                Path,
                                serverPaths,
                                DataTypeInfo.PathInfo,
                                Step.SYNC_PATHS
                            ) {}
                        }
                    }
                }
            } ?: Timber.w("Could not get a valid data tree.")
        }

        syncBlocks()

        // Synchronize deletions for type
        val apiKey = Preferences.getApiKey(applicationContext).firstOrNull()
        if (apiKey != null) {
            // Remove from bottom to top so that restrictions do not break
            performDeletions(apiKey, LocalDeletion.TYPE_BLOCK, null)
            performDeletions(apiKey, LocalDeletion.TYPE_PATH, serverPaths)
            performDeletions(apiKey, LocalDeletion.TYPE_SECTOR, serverSectors)
            performDeletions(apiKey, LocalDeletion.TYPE_ZONE, serverZones)
            performDeletions(apiKey, LocalDeletion.TYPE_AREA, serverAreas)
        }

        // Synchronize deletions
        setProgress(Step.DELETE_AREAS)
        synchronizeDeletions(serverAreas, dao::getAllAreas) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted areas from server.") }
        setProgress(Step.DELETE_ZONES)
        synchronizeDeletions(serverZones, dao::getAllZones) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted zones from server.") }
        setProgress(Step.DELETE_SECTORS)
        synchronizeDeletions(serverSectors, dao::getAllSectors) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted sectors from server.") }
        setProgress(Step.DELETE_PATHS)
        synchronizeDeletions(serverPaths, dao::getAllPaths) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted paths from server.") }
        setProgress(Step.DELETE_BLOCKING)
        synchronizeDeletions(serverBlockings, dao::getAllBlocks) { dao.delete(it) }
            .let { Timber.d("Synchronized $it deleted blockings from server.") }
    }

    private suspend fun syncBlocks() {
        Timber.d("Getting all blocks...")

        get(EndpointUtils.getUrl("blocks")) { data ->
            val blocks = data?.getJSONArray("blocks") ?: JSONArray()
            if (blocks.length() <= 0) {
                Timber.d("- There aren't any blocks in the server.")
            } else {
                Timber.d("- Found ${blocks.length()} blocks, synchronizing...")
                blocks.serialize(Blocking).forEach { serverBlocking ->
                    val localBlocking = dao.getBlocking(serverBlocking.id)

                    Timber.d("  Patching Blocking#${serverBlocking.id}...")
                    patch(localBlocking, serverBlocking, DataTypeInfo.BlockingInfo)

                    serverBlockings += serverBlocking.id
                }
            }
        }
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

    private suspend fun setProgress(step: Step, progress: Int? = null, max: Int? = null) {
        Timber.d("Progress: ${step.name} $progress / $max")
        setProgress(
            workDataOf(
                PROGRESS_STEP to step.name,
                PROGRESS_PROGRESS to progress,
                PROGRESS_MAX to max
            )
        )
    }

    /**
     * Checks all the update times to consider running synchronization or not.
     */
    private suspend fun shouldRunSynchronization(transaction: ITransaction): Boolean {
        if (inputData.getBoolean(DATA_FORCE, false)) {
            return true
        }

        get(EndpointUtils.getUrl("last_update")) { data ->
            if (data == null) {
                Timber.w("Last update from server didn't return any data. Running sync...")
                transaction.setData("sync_reason", "no_last_update")
                return true
            }
            val lastUpdate = data.getLongOrNull("last_update")?.let(Instant::ofEpochMilli)
            if (lastUpdate == null) {
                Timber.w("Last update from server is null. Running sync...")
                transaction.setData("sync_reason", "null_last_update")
                return true
            }
            val localLastUpdate = Preferences.getLastUpdate(applicationContext)
                .firstOrNull()
            val localLastModification = Preferences.getLastModification(applicationContext)
                .firstOrNull()

            if (localLastUpdate == null) {
                Timber.i("No last update stored locally, running sync...")
                transaction.setData("sync_reason", "no_local_last_update")
            } else if (localLastUpdate < lastUpdate) {
                Timber.i("Server has been updated, running sync...")
                transaction.setData("sync_reason", "server_updated")
            } else if (localLastModification != null && localLastModification > localLastUpdate) {
                Timber.i("Local data has been modified after local, running sync...")
                transaction.setData("sync_reason", "local_modified")
            } else if (localLastModification != null && localLastModification > lastUpdate) {
                Timber.i("Local data has been modified after server, running sync...")
                transaction.setData("sync_reason", "local_modified_after_server")
            } else {
                Timber.i("Local data up to date with server. Won't run sync.")
                transaction.setData("sync_reason", "up_to_date")
                transaction.status = SpanStatus.ABORTED
                return false
            }
            return true
        }
        return true
    }

    enum class Step {
        GET_TREE,
        SYNC_AREAS,
        SYNC_ZONES,
        SYNC_SECTORS,
        SYNC_PATHS,
        DELETE_AREAS,
        DELETE_ZONES,
        DELETE_SECTORS,
        DELETE_PATHS,
        DELETE_BLOCKING
    }

    enum class StopReason {
        ALREADY_UP_TO_DATE,
        SYNC_COMPLETED,
        ERROR
    }
}
