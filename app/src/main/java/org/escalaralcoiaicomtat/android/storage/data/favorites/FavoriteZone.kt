package org.escalaralcoiaicomtat.android.storage.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_zones")
data class FavoriteZone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val zoneId: Long
): Favorite
