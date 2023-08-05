package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import java.time.Instant

@Entity(tableName = "paths")
@Suppress("LongParameterList")
data class Path(
    @PrimaryKey
    override val id: Long = 0L,
    override val timestamp: Instant,
    val displayName: String,
    val sketchId: Long,

    val height: Long?,
    val grade: GradeValue?,

    val pitches: List<PitchInfo>?,

    val stringCount: Long?,

    val paraboltCount: Long?,
    val burilCount: Long?,
    val pitonCount: Long?,
    val spitCount: Long?,
    val tensorCount: Long?,

    val crackerRequired: Boolean,
    val friendRequired: Boolean,
    val lanyardRequired: Boolean,
    val nailRequired: Boolean,
    val pitonRequired: Boolean,
    val stapesRequired: Boolean,

    val showDescription: Boolean,
    val description: String?,

    val builder: Builder?,
    val reBuilder: List<Builder>?,

    val sectorId: Long
) : BaseEntity()
