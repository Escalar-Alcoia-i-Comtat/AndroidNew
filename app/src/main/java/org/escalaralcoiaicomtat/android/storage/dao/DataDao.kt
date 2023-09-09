package org.escalaralcoiaicomtat.android.storage.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
interface DataDao {
    @WorkerThread
    @Insert
    suspend fun insert(items: Area): Long

    @WorkerThread
    @Delete
    suspend fun delete(items: Area)

    @WorkerThread
    @Update
    suspend fun update(items: Area)

    @WorkerThread
    @Query("SELECT * FROM areas WHERE id=:id")
    suspend fun getArea(id: Long): Area?

    @WorkerThread
    @Query("SELECT * FROM areas")
    suspend fun getAllAreas(): List<Area>

    @WorkerThread
    @Query("SELECT * FROM areas")
    fun getAllAreasLive(): LiveData<List<Area>>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM areas WHERE id=:areaId")
    fun getZonesFromAreaLive(areaId: Long): LiveData<AreaWithZones>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM areas WHERE id=:areaId")
    suspend fun getZonesFromArea(areaId: Long): AreaWithZones?


    @WorkerThread
    @Insert
    suspend fun insert(items: Zone): Long

    @WorkerThread
    @Delete
    suspend fun delete(items: Zone)

    @WorkerThread
    @Update
    suspend fun update(items: Zone)

    @WorkerThread
    @Query("SELECT * FROM zones WHERE id=:id")
    suspend fun getZone(id: Long): Zone?

    @WorkerThread
    @Query("SELECT * FROM zones")
    suspend fun getAllZones(): List<Zone>

    @WorkerThread
    @Query("SELECT * FROM zones")
    fun getAllZonesLive(): LiveData<List<Zone>>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM zones WHERE id=:zoneId")
    fun getSectorsFromZoneLive(zoneId: Long): LiveData<ZoneWithSectors>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM zones WHERE id=:zoneId")
    suspend fun getSectorsFromZone(zoneId: Long): ZoneWithSectors?


    @WorkerThread
    @Insert
    suspend fun insert(items: Sector): Long

    @WorkerThread
    @Delete
    suspend fun delete(items: Sector)

    @WorkerThread
    @Update
    suspend fun update(items: Sector)

    @WorkerThread
    @Query("SELECT * FROM sectors WHERE id=:id")
    suspend fun getSector(id: Long): Sector?

    @WorkerThread
    @Query("SELECT * FROM sectors")
    suspend fun getAllSectors(): List<Sector>

    @WorkerThread
    @Query("SELECT * FROM sectors")
    fun getAllSectorsLive(): LiveData<List<Sector>>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM sectors WHERE id=:sectorId")
    fun getPathsFromSectorLive(sectorId: Long): LiveData<SectorWithPaths?>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM sectors WHERE id=:sectorId")
    suspend fun getPathsFromSector(sectorId: Long): SectorWithPaths?


    @WorkerThread
    @Insert
    suspend fun insert(items: Path): Long

    @WorkerThread
    @Delete
    suspend fun delete(items: Path)

    @WorkerThread
    @Update
    suspend fun update(items: Path)

    @WorkerThread
    @Query("SELECT * FROM paths WHERE id=:id")
    suspend fun getPath(id: Long): Path?

    @WorkerThread
    @Query("SELECT * FROM paths")
    suspend fun getAllPaths(): List<Path>

    @WorkerThread
    @Query("SELECT * FROM paths")
    fun getAllPathsLive(): LiveData<List<Path>>

    @WorkerThread
    @Transaction
    @Query("SELECT * FROM paths WHERE parentId=:sectorId")
    suspend fun getPathWithBlocks(sectorId: Long): List<PathWithBlocks>

    @Transaction
    @Query("SELECT * FROM paths WHERE parentId=:sectorId")
    fun getPathWithBlocksLive(sectorId: Long): LiveData<List<PathWithBlocks>>

    @Query("SELECT builder FROM paths")
    suspend fun getAllBuilders(): List<String>

    @Query("SELECT reBuilder FROM paths")
    suspend fun getAllReBuilders(): List<String>


    @WorkerThread
    @Insert
    suspend fun insert(items: Blocking): Long

    @WorkerThread
    @Delete
    suspend fun delete(items: Blocking)

    @WorkerThread
    @Update
    suspend fun update(items: Blocking)

    @WorkerThread
    @Query("SELECT * FROM blocking WHERE id=:id")
    suspend fun getBlocking(id: Long): Blocking?

    @WorkerThread
    @Query("SELECT * FROM blocking")
    suspend fun getAllBlocks(): List<Blocking>

    @WorkerThread
    @Query("SELECT * FROM blocking WHERE parentId=:path")
    suspend fun getAllBlocks(path: Long): List<Blocking>


    @WorkerThread
    @Query("SELECT * FROM local_deletions WHERE type=:type")
    suspend fun pendingDeletions(type: String): List<LocalDeletion>

    @Insert
    @WorkerThread
    suspend fun notifyDeletion(delete: LocalDeletion): Long

    @Delete
    @WorkerThread
    suspend fun clearDeletion(delete: LocalDeletion)
}
