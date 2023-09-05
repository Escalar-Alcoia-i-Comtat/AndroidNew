package org.escalaralcoiaicomtat.android.storage.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Stores the data of a deleted object for synchronizing with server.
 *
 * @param type The type of element to delete, this is the base endpoint of the DELETE request.
 * @param deleteId The id of the element to delete.
 */
@Entity(tableName = "local_deletions")
data class LocalDeletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,
    val deleteId: Long
) {
    companion object {
        const val TYPE_AREA = "area"
        const val TYPE_ZONE = "zone"
        const val TYPE_SECTOR = "sector"
        const val TYPE_PATH = "path"

        fun fromArea(area: Area) = LocalDeletion(0L, TYPE_AREA, area.id)
        fun fromZone(zone: Zone) = LocalDeletion(0L, TYPE_ZONE, zone.id)
        fun fromSector(sector: Sector) = LocalDeletion(0L, TYPE_SECTOR, sector.id)
        fun fromPath(path: Path) = LocalDeletion(0L, TYPE_PATH, path.id)
    }

    @Ignore
    val endpoint = "/$type/$deleteId"
}
