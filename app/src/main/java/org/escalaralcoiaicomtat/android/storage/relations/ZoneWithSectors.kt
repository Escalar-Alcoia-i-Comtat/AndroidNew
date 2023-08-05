package org.escalaralcoiaicomtat.android.storage.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone

data class ZoneWithSectors(
    @Embedded val zone: Zone,
    @Relation(parentColumn = "id", entityColumn = "zoneId")
    val sectors: List<Sector>
)
