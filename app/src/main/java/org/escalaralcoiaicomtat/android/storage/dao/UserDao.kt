package org.escalaralcoiaicomtat.android.storage.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteArea
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteSector
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteZone

@Dao
interface UserDao {
    @WorkerThread
    @Insert
    suspend fun insert(item: FavoriteArea): Long

    @WorkerThread
    @Delete
    suspend fun delete(item: FavoriteArea)

    @WorkerThread
    @Query("SELECT * FROM favorite_areas WHERE areaId=:areaId")
    suspend fun getArea(areaId: Long): FavoriteArea?

    @Query("SELECT * FROM favorite_areas WHERE areaId=:areaId")
    fun getAreaLive(areaId: Long): LiveData<FavoriteArea?>

    @Query("SELECT * FROM favorite_areas")
    fun getAllAreasLive(): LiveData<List<FavoriteArea>>


    @WorkerThread
    @Insert
    suspend fun insert(item: FavoriteZone): Long

    @WorkerThread
    @Delete
    suspend fun delete(item: FavoriteZone)

    @WorkerThread
    @Query("SELECT * FROM favorite_zones WHERE zoneId=:zoneId")
    suspend fun getZone(zoneId: Long): FavoriteZone?

    @Query("SELECT * FROM favorite_zones WHERE zoneId=:zoneId")
    fun getZoneLive(zoneId: Long): LiveData<FavoriteZone?>

    @Query("SELECT * FROM favorite_zones")
    fun getAllZonesLive(): LiveData<List<FavoriteZone>>


    @WorkerThread
    @Insert
    suspend fun insert(item: FavoriteSector): Long

    @WorkerThread
    @Delete
    suspend fun delete(item: FavoriteSector)

    @WorkerThread
    @Query("SELECT * FROM favorite_sectors WHERE sectorId=:sectorId")
    suspend fun getSector(sectorId: Long): FavoriteSector?

    @Query("SELECT * FROM favorite_sectors WHERE sectorId=:sectorId")
    fun getSectorLive(sectorId: Long): LiveData<FavoriteSector?>

    @Query("SELECT * FROM favorite_sectors")
    fun getAllSectorsLive(): LiveData<List<FavoriteSector>>

}
