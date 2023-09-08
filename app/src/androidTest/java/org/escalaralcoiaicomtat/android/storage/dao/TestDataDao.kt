package org.escalaralcoiaicomtat.android.storage.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.escalaralcoiaicomtat.android.SampleDataProvider
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestDataDao {
    private lateinit var db: AppDatabase
    private lateinit var dao: DataDao

    @Before
    fun create_db() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AppDatabase.instance = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .build()
            .also { db = it }
        dao = db.dataDao()
    }

    @After
    fun close_db() {
        db.close()
    }

    @Test
    fun test_recursiveDeletion() {
        runBlocking {
            val areaId = dao.insert(SampleDataProvider.SampleArea)
            val zoneId = dao.insert(SampleDataProvider.SampleZone.copy(parentId = areaId))
            val sectorId = dao.insert(SampleDataProvider.SampleSector.copy(parentId = zoneId))
            val pathId = dao.insert(SampleDataProvider.SamplePath.copy(parentId = sectorId))

            val area = dao.getArea(areaId)
            dao.deleteRecursively(area!!)

            val pendingAreas = dao.pendingDeletions(LocalDeletion.TYPE_AREA)
            assert(pendingAreas.size == 1)
            assert(pendingAreas[0].deleteId == areaId)

            val pendingZones = dao.pendingDeletions(LocalDeletion.TYPE_ZONE)
            assert(pendingZones.size == 1)
            assert(pendingZones[0].deleteId == zoneId)

            val pendingSectors = dao.pendingDeletions(LocalDeletion.TYPE_SECTOR)
            assert(pendingSectors.size == 1)
            assert(pendingSectors[0].deleteId == sectorId)

            val pendingPaths = dao.pendingDeletions(LocalDeletion.TYPE_PATH)
            assert(pendingPaths.size == 1)
            assert(pendingPaths[0].deleteId == pathId)
        }
    }
}
