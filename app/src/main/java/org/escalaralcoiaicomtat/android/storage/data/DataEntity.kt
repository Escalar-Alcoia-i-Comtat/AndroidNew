package org.escalaralcoiaicomtat.android.storage.data

abstract class DataEntity : BaseEntity() {
    abstract val displayName: String
    abstract val isFavorite: Boolean
}
