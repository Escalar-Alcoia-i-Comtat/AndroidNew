package org.escalaralcoiaicomtat.android.storage.data

import java.time.Instant

abstract class BaseEntity {
    abstract val id: Long
    abstract val timestamp: Instant
}
