package org.escalaralcoiaicomtat.android.storage.data

/**
 * Returns the given list sorted by the type of [G]. Supported types:
 * - [Area]
 * - [Zone]
 * - [Sector]
 * - [Path]
 */
fun <G : BaseEntity> Iterable<G>.sorted(): List<G> =
    if (any()) {
        when (first()) {
            is Area -> sortedBy { (it as Area).displayName }

            is Zone -> sortedBy { (it as Zone).displayName }

            is Sector -> sortedWith(
                compareBy(
                    { (it as Sector).weight },
                    { sector ->
                        sector as Sector
                        if (sector.isFavorite) {
                            "\u0000${sector.displayName}"
                        } else {
                            sector.displayName
                        }
                    }
                )
            )

            is Path -> sortedBy { (it as Path).sketchId }

            is ImageEntity -> sortedBy { (it as ImageEntity).displayName }

            is DataEntity -> sortedBy { (it as DataEntity).displayName }

            else -> sortedBy { it.id }
        }
    } else {
        emptyList()
    }
