package org.escalaralcoiaicomtat.android.storage.data

import java.time.Instant
import kotlin.reflect.full.memberProperties

abstract class BaseEntity {
    abstract val id: Long
    abstract val timestamp: Instant
}

data class EntityProperty(
    val name: String,
    val type: String,
    val value: Any?
)

/**
 * Gets a list of all the properties the entity contains.
 *
 * All fields named `*Id` will have `Id` removed.
 */
inline fun <reified E : BaseEntity> E.properties() =
    E::class.memberProperties.map { prop ->
        EntityProperty(
            prop.name.let {
                if (it.endsWith("Id"))
                    it.substringBeforeLast("Id")
                else
                    it
            },
            prop.returnType.toString().substringAfterLast('.'),
            prop.get(this)
        )
    }

data class EntityPropertyPair(
    val name: String,
    val value1: Any?,
    val value2: Any?
)

/**
 * Gets a list of all the properties the entity contains.
 *
 * All fields named `*Id` will have `Id` removed.
 */
inline infix fun <reified E : BaseEntity> E.propertiesWith(other: E): List<EntityPropertyPair> =
    E::class.memberProperties.map { prop ->
        EntityPropertyPair(
            prop.name.let {
                if (it.endsWith("Id"))
                    it.substringBeforeLast("Id")
                else
                    it
            },
            prop.get(this),
            prop.get(other)
        )
    }
