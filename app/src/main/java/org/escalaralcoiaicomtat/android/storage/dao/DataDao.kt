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
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Blocking
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
    suspend fun insert(vararg items: Area)

    @WorkerThread
    @Delete
    suspend fun delete(vararg items: Area)

    @WorkerThread
    @Update
    suspend fun update(vararg items: Area)

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
    suspend fun insert(vararg items: Zone)

    @WorkerThread
    @Delete
    suspend fun delete(vararg items: Zone)

    @WorkerThread
    @Update
    suspend fun update(vararg items: Zone)

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
    suspend fun insert(vararg items: Sector)

    @WorkerThread
    @Delete
    suspend fun delete(vararg items: Sector)

    @WorkerThread
    @Update
    suspend fun update(vararg items: Sector)

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
    suspend fun getPathsFromSector(sectorId: Long): SectorWithPaths?


    @WorkerThread
    @Insert
    suspend fun insert(vararg items: Path)

    @WorkerThread
    @Delete
    suspend fun delete(vararg items: Path)

    @WorkerThread
    @Update
    suspend fun update(vararg items: Path)

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
    @Query("SELECT * FROM paths")
    suspend fun getPathWithBlocks(): List<PathWithBlocks>

    @Query("SELECT builder FROM paths")
    suspend fun getAllBuilders(): List<String>

    @Query("SELECT reBuilder FROM paths")
    suspend fun getAllReBuilders(): List<String>


    @WorkerThread
    @Insert
    suspend fun insert(vararg items: Blocking)

    @WorkerThread
    @Delete
    suspend fun delete(vararg items: Blocking)

    @WorkerThread
    @Update
    suspend fun update(vararg items: Blocking)

    @WorkerThread
    @Query("SELECT * FROM blocking")
    suspend fun getAllBlocks(): List<Blocking>

    @WorkerThread
    @Query("SELECT * FROM blocking WHERE pathId=:path")
    suspend fun getAllBlocks(path: Long): List<Blocking>
}

/**
 * Deletes [element] from the database, and all of its children recursively.
 */
suspend fun <Type: BaseEntity> DataDao.deleteRecursively(element: Type) {
    when (element) {
        is Area -> {
            delete(element)
            getZonesFromArea(element.id)?.zones?.forEach {
                deleteRecursively(it)
            }
        }
        is Zone -> {
            delete(element)
            getSectorsFromZone(element.id)?.sectors?.forEach {
                deleteRecursively(it)
            }
        }
        is Sector -> {
            delete(element)
            getPathsFromSector(element.id)?.paths?.forEach {
                deleteRecursively(it)
            }
        }
        is Path -> delete(element)
    }
}
