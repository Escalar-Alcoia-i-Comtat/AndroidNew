package org.escalaralcoiaicomtat.android.utils

fun <T> T.letIf(condition: Boolean, block: (T) -> T) =
    if (condition) {
        block(this)
    } else {
        this
    }

fun <T, O> T.letIfNotNull(obj: O?, block: T.(O) -> T) =
    if (obj != null) {
        block(this, obj)
    } else {
        this
    }
