package org.escalaralcoiaicomtat.android.storage.data.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_areas")
data class FavoriteArea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val areaId: Long
): Favorite
