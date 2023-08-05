package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.storage.type.BlockingRecurrenceYearly
import org.escalaralcoiaicomtat.android.storage.type.BlockingTypes
import java.time.Instant
import java.time.ZonedDateTime

@Entity(tableName = "blocking")
data class Blocking(
    @PrimaryKey
    override val id: Long = 0L,
    override val timestamp: Instant,
    val type: BlockingTypes,
    val recurrence: BlockingRecurrenceYearly,
    val endDate: ZonedDateTime,
    val pathId: Long
) : BaseEntity()
