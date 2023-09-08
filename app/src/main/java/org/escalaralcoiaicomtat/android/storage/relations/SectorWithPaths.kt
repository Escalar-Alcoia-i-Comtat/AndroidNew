package org.escalaralcoiaicomtat.android.storage.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector

data class SectorWithPaths(
    @Embedded val sector: Sector,
    @Relation(parentColumn = "id", entityColumn = "parentId")
    val paths: List<Path>
)
