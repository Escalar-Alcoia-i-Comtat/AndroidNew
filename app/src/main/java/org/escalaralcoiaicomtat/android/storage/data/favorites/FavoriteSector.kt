package org.escalaralcoiaicomtat.android.storage.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_sectors")
data class FavoriteSector(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sectorId: Long
): Favorite
