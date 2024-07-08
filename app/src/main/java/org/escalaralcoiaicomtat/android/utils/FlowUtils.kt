package org.escalaralcoiaicomtat.android.utils

inline fun <T> T.letIf(condition: Boolean, block: (T) -> T) =
    if (condition) {
        block(this)
    } else {
        this
    }

inline fun <T, O> T.letIfNotNull(obj: O?, block: T.(O) -> T) =
    if (obj != null) {
        block(this, obj)
    } else {
        this
    }

/**
 * Alias for [String.takeIf] that checks if the string is not blank.
 * ```kotlin
 * takeIf { it.isNotBlank() }
 * ```
 */
fun String?.takeIfNotBlank(): String? = this?.takeIf { it.isNotBlank() }
