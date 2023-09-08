package org.escalaralcoiaicomtat.android.storage.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Zone

data class AreaWithZones(
    @Embedded val area: Area,
    @Relation(parentColumn = "id", entityColumn = "parentId")
    val zones: List<Zone>
)
