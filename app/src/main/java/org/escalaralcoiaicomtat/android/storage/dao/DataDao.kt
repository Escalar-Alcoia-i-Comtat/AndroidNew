package org.escalaralcoiaicomtat.android.storage.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.relations.AreaWithZones
import org.escalaralcoiaicomtat.android.storage.relations.PathWithBlocks
import org.escalaralcoiaicomtat.android.storage.relations.SectorWithPaths
import org.escalaralcoiaicomtat.android.storage.relations.ZoneWithSectors

@Dao
@Suppress("TooManyFunctions")
abstract class DataDao {
    @WorkerThread
    @Insert
    abstract suspend fun insert(items: Area): Long

    @WorkerThread
    @Delete
    abstract suspend fun delete(items: Area)

    @WorkerThread
    @Update
    abstract suspend fun update(items: Area)

    @WorkerThread
    @Query("SELECT * FROM areas WHERE id=:id")
    abstract suspend fun getArea(id: Long): Area?

    @WorkerThread
    @Query("SELECT * FROM areas WHERE image=:uuid")
    abstract suspend fun getAreaByImage(uuid: String): Area?

    @WorkerThread
    @Query("SELECT * FROM areas")
    abstract suspend fun getAllAreas(): List<Area>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getAllAreasFlow()"))
    @Query("SELECT * FROM areas")
    abstract fun getAllAreasLive(): LiveData<List<Area>>

    @Query("SELECT * FROM areas")
    abstract fun getAllAreasFlow(): Flow<List<Area>>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getZonesFromAreaFlow(areaId)"))
    @Transaction
    @Query("SELECT * FROM areas WHERE id=:areaId")
    abstract fun getZonesFromAreaLive(areaId: Long): LiveData<AreaWithZones>

    @Transaction
    @Query("SELECT * FROM areas WHERE id=:areaId")
    abstract fun getZonesFromAreaFlow(areaId: Long): Flow<AreaWithZones>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM areas WHERE id=:areaId")
    abstract suspend fun getZonesFromArea(areaId: Long): AreaWithZones?


    @WorkerThread
    @Insert
    abstract suspend fun insert(items: Zone): Long

    @WorkerThread
    @Delete
    abstract suspend fun delete(items: Zone)

    @WorkerThread
    @Update
    abstract suspend fun update(items: Zone)

    @WorkerThread
    @Query("SELECT * FROM zones WHERE id=:id")
    abstract suspend fun getZone(id: Long): Zone?

    @WorkerThread
    @Query("SELECT * FROM zones WHERE image=:uuid")
    abstract suspend fun getZoneByImage(uuid: String): Zone?

    @WorkerThread
    @Query("SELECT * FROM zones")
    abstract suspend fun getAllZones(): List<Zone>

    @WorkerThread
    @Query("SELECT * FROM zones")
    abstract fun getAllZonesLive(): LiveData<List<Zone>>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getSectorsFromZoneFlow(zoneId)"))
    @Transaction
    @Query("SELECT * FROM zones WHERE id=:zoneId")
    abstract fun getSectorsFromZoneLive(zoneId: Long): LiveData<ZoneWithSectors>

    @Transaction
    @Query("SELECT * FROM zones WHERE id=:zoneId")
    abstract fun getSectorsFromZoneFlow(zoneId: Long): Flow<ZoneWithSectors>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM zones WHERE id=:zoneId")
    abstract suspend fun getSectorsFromZone(zoneId: Long): ZoneWithSectors?


    @WorkerThread
    @Insert
    abstract suspend fun insert(items: Sector): Long

    @WorkerThread
    @Delete
    abstract suspend fun delete(items: Sector)

    @WorkerThread
    @Update
    abstract suspend fun update(items: Sector)

    @WorkerThread
    @Query("SELECT * FROM sectors WHERE id=:id")
    abstract suspend fun getSector(id: Long): Sector?

    @WorkerThread
    @Query("SELECT * FROM sectors WHERE image=:uuid")
    abstract suspend fun getSectorByImage(uuid: String): Sector?

    @WorkerThread
    @Query("SELECT * FROM sectors")
    abstract suspend fun getAllSectors(): List<Sector>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getAllSectorsFlow()"))
    @Query("SELECT * FROM sectors")
    abstract fun getAllSectorsLive(): LiveData<List<Sector>>

    @Query("SELECT * FROM sectors")
    abstract fun getAllSectorsFlow(): Flow<List<Sector>>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getPathsFromSectorFlow(sectorId)"))
    @Transaction
    @Query("SELECT * FROM sectors WHERE id=:sectorId")
    abstract fun getPathsFromSectorLive(sectorId: Long): LiveData<SectorWithPaths?>

    @Transaction
    @Query("SELECT * FROM sectors WHERE id=:sectorId")
    abstract fun getPathsFromSectorFlow(sectorId: Long): Flow<SectorWithPaths?>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM sectors WHERE id=:sectorId")
    abstract suspend fun getPathsFromSector(sectorId: Long): SectorWithPaths?


    @WorkerThread
    @Insert
    abstract suspend fun insert(items: Path): Long

    @WorkerThread
    @Delete
    abstract suspend fun delete(items: Path)

    @WorkerThread
    @Update
    abstract suspend fun update(items: Path)

    @WorkerThread
    @Query("SELECT * FROM paths WHERE id=:id")
    abstract suspend fun getPath(id: Long): Path?

    @WorkerThread
    @Query("SELECT * FROM paths")
    abstract suspend fun getAllPaths(): List<Path>

    @WorkerThread
    @Query("SELECT * FROM paths")
    abstract fun getAllPathsLive(): LiveData<List<Path>>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM paths WHERE parentId=:sectorId")
    abstract suspend fun getPathWithBlocks(sectorId: Long): List<PathWithBlocks>

    @Deprecated("Use Flow", replaceWith = ReplaceWith("getPathWithBlocksFlow(sectorId)"))
    @Transaction
    @Query("SELECT * FROM paths WHERE parentId=:sectorId")
    abstract fun getPathWithBlocksLive(sectorId: Long): LiveData<List<PathWithBlocks>>

    @Transaction
    @Query("SELECT * FROM paths WHERE parentId=:sectorId")
    abstract fun getPathWithBlocksFlow(sectorId: Long): Flow<List<PathWithBlocks>>

    @Query("SELECT builder FROM paths")
    abstract suspend fun getAllBuilders(): List<String>

    @Query("SELECT reBuilder FROM paths")
    abstract suspend fun getAllReBuilders(): List<String>


    @WorkerThread
    @Insert
    abstract suspend fun insert(items: Blocking): Long

    @WorkerThread
    @Delete
    abstract suspend fun delete(items: Blocking)

    @WorkerThread
    @Update
    abstract suspend fun update(items: Blocking)

    @WorkerThread
    @Query("SELECT * FROM blocking WHERE id=:id")
    abstract suspend fun getBlocking(id: Long): Blocking?

    @WorkerThread
    @Query("SELECT * FROM blocking")
    abstract suspend fun getAllBlocks(): List<Blocking>

    @WorkerThread
    @Query("SELECT * FROM blocking WHERE parentId=:path")
    abstract suspend fun getAllBlocks(path: Long): List<Blocking>


    @WorkerThread
    @Query("SELECT * FROM local_deletions WHERE type=:type")
    abstract suspend fun pendingDeletions(type: String): List<LocalDeletion>

    @Insert
    @WorkerThread
    abstract suspend fun notifyDeletion(delete: LocalDeletion): Long

    @Delete
    @WorkerThread
    abstract suspend fun clearDeletion(delete: LocalDeletion)

    @WorkerThread
    @Transaction
    open suspend fun deleteByImageUUID(uuid: String) {
        getAreaByImage(uuid)?.let { delete(it) }
        getZoneByImage(uuid)?.let { delete(it) }
        getSectorByImage(uuid)?.let { delete(it) }
    }
}
