package org.escalaralcoiaicomtat.android

import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.junit.Test
import java.time.Instant
import org.escalaralcoiaicomtat.android.storage.data.sorted

class TestBaseEntity {
    @Test
    fun test_BaseEntity_equals() {
        val entity1: BaseEntity = object : BaseEntity() {
            override val parentId: Long = -1
            override val id: Long = 1
            override val timestamp: Instant = Instant.ofEpochMilli(123)
        }
        val entity2: BaseEntity = object : BaseEntity() {
            override val parentId: Long = -1
            override val id: Long = 1
            override val timestamp: Instant = Instant.ofEpochMilli(321)
        }
        val entity3: BaseEntity = object : BaseEntity() {
            override val parentId: Long = -1
            override val id: Long = 2
            override val timestamp: Instant = Instant.ofEpochMilli(123)
        }

        assert(entity1 == entity1)
        // assert(entity1 == entity2)
        assert(entity1 != entity3)
    }

    @Test
    fun test_BaseEntity_sorted() {
        val areas = listOf(
            SampleDataProvider.SampleArea.copy(id = 3, displayName = "CAB"),
            SampleDataProvider.SampleArea.copy(id = 1, displayName = "ABC"),
            SampleDataProvider.SampleArea.copy(id = 2, displayName = "BCA"),
        ).sorted()
        assert(areas[0].id == 1L)
        assert(areas[1].id == 2L)
        assert(areas[2].id == 3L)

        val zones = listOf(
            SampleDataProvider.SampleZone.copy(id = 3, displayName = "CAB"),
            SampleDataProvider.SampleZone.copy(id = 1, displayName = "ABC"),
            SampleDataProvider.SampleZone.copy(id = 2, displayName = "BCA"),
        ).sorted()
        assert(zones[0].id == 1L)
        assert(zones[1].id == 2L)
        assert(zones[2].id == 3L)

        val sectors = listOf(
            SampleDataProvider.SampleSector.copy(id = 3, displayName = "CAB", weight = "aaa"),
            SampleDataProvider.SampleSector.copy(id = 6, displayName = "BCA", weight = "aad"),
            SampleDataProvider.SampleSector.copy(id = 1, displayName = "ABC", weight = "aaa"),
            SampleDataProvider.SampleSector.copy(id = 4, displayName = "CAB", weight = "aab"),
            SampleDataProvider.SampleSector.copy(id = 2, displayName = "BCA", weight = "aaa"),
            SampleDataProvider.SampleSector.copy(id = 5, displayName = "ABC", weight = "aac"),
        ).sorted()
        assert(sectors[0].id == 1L)
        assert(sectors[1].id == 2L)
        assert(sectors[2].id == 3L)
        assert(sectors[3].id == 4L)
        assert(sectors[4].id == 5L)
        assert(sectors[5].id == 6L)

        val paths = listOf(
            SampleDataProvider.SamplePath.copy(id = 3, sketchId = 3),
            SampleDataProvider.SamplePath.copy(id = 1, sketchId = 1),
            SampleDataProvider.SamplePath.copy(id = 2, sketchId = 2),
        ).sorted()
        assert(paths[0].id == 1L)
        assert(paths[1].id == 2L)
        assert(paths[2].id == 3L)
    }
}
