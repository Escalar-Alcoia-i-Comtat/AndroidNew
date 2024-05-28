package org.escalaralcoiaicomtat.android.utils

import org.escalaralcoiaicomtat.android.storage.data.DataEntity

fun <DE: DataEntity> List<DE>.filterBySearchQuery(query: String): List<DE> {
    val normalizedQuery = query.lowercase().normalized
    return filter {
        it.displayName.lowercase().normalized.contains(normalizedQuery, ignoreCase = true)
    }
}
