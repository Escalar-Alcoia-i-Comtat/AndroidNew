package org.escalaralcoiaicomtat.android.storage.dao

import androidx.annotation.WorkerThread
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteArea
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteSector
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteZone

@WorkerThread
suspend fun UserDao.toggleFavorite(area: Area) {
    val favorite = getArea(area.id)
    if (favorite != null) {
        delete(favorite)
    } else {
        insert(
            FavoriteArea(areaId = area.id)
        )
    }
}

@WorkerThread
suspend fun UserDao.toggleFavorite(zone: Zone) {
    val favorite = getZone(zone.id)
    if (favorite != null) {
        delete(favorite)
    } else {
        insert(
            FavoriteZone(zoneId = zone.id)
        )
    }
}

@WorkerThread
suspend fun UserDao.toggleFavorite(sector: Sector) {
    val favorite = getSector(sector.id)
    if (favorite != null) {
        delete(favorite)
    } else {
        insert(
            FavoriteSector(sectorId = sector.id)
        )
    }
}
