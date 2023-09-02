package org.escalaralcoiaicomtat.android.storage.dao

import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.utils.serialize
import org.json.JSONArray
import org.json.JSONObject

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
