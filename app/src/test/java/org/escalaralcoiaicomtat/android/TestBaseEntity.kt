package org.escalaralcoiaicomtat.android

import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.junit.Test
import java.time.Instant

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
}
