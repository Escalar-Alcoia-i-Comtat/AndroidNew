package org.escalaralcoiaicomtat.android.storage.dao

import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.utils.filterBySearchQuery
import org.escalaralcoiaicomtat.android.utils.serialize
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

/**
 * Gets all the builders and re-builders in the database and returns then without repeating.
 */
suspend fun DataDao.getBuildersSet(): Set<String> {
    val builders = getAllBuilders()
        .map { JSONObject(it) }
        .map(Builder::fromJson)
        .mapNotNull { it.name }
    val reBuilders = JSONArray(getAllReBuilders())
        .serialize(Builder)
        .mapNotNull { it.name }

    return setOf(
        *builders.toTypedArray(),
        *reBuilders.toTypedArray()
    )
}

/**
 * Deletes [element] from the database, and all of its children recursively.
 */
suspend fun <Type: BaseEntity> DataDao.deleteRecursively(element: Type) {
    when (element) {
        is Area -> {
            Timber.d("Deleting Area#${element.id}...")
            getZonesFromArea(element.id)?.zones?.forEach {
                deleteRecursively(it)
            } ?: Timber.d("Area#${element.id} doesn't have any children.")
            notifyDeletion(LocalDeletion.fromArea(element))
            delete(element)
        }
        is Zone -> {
            Timber.d("Deleting Zone#${element.id}...")
            getSectorsFromZone(element.id)?.sectors?.forEach {
                deleteRecursively(it)
            } ?: Timber.d("Zone#${element.id} doesn't have any children.")
            notifyDeletion(LocalDeletion.fromZone(element))
            delete(element)
        }
        is Sector -> {
            Timber.d("Deleting Sector#${element.id}...")
            getPathsFromSector(element.id)?.paths?.forEach {
                deleteRecursively(it)
            } ?: Timber.d("Sector#${element.id} doesn't have any children.")
            notifyDeletion(LocalDeletion.fromSector(element))
            delete(element)
        }
        is Path -> {
            Timber.d("Deleting Path#${element.id}...")
            notifyDeletion(LocalDeletion.fromPath(element))
            delete(element)
        }
    }
}

/**
 * Searches for [query] in the database and returns all the entities that contain it in their display name.
 */
suspend fun DataDao.search(query: String): List<DataEntity> {
    val areas = getAllAreas().filterBySearchQuery(query)
    val zones = getAllZones().filterBySearchQuery(query)
    val sectors = getAllSectors().filterBySearchQuery(query)
    val paths = getAllPaths().filterBySearchQuery(query)

    return areas + zones + sectors + paths
}
