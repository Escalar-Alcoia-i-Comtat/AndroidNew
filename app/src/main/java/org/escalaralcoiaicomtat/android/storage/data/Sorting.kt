package org.escalaralcoiaicomtat.android.storage.data

/**
 * Returns the given list sorted by the type of [G]. Supported types:
 * - [Area]
 * - [Zone]
 * - [Sector]
 * - [Path]
 */
inline fun <reified G : BaseEntity> Iterable<G>.sorted(): List<G> =
    if (any()) {
        when (G::class) {
            Area::class -> sortedBy { (it as Area).displayName }

            Zone::class -> sortedBy { (it as Zone).displayName }

            Sector::class -> sortedWith(
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

            Path::class -> sortedBy { (it as Path).sketchId }

            else -> throw IllegalArgumentException(
                "Got an invalid BaseEntity type: ${G::class.simpleName}"
            )
        }
    } else {
        emptyList()
    }
