package org.escalaralcoiaicomtat.android.storage.data

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource

abstract class DataEntity : BaseEntity() {
    abstract val displayName: String
    abstract val isFavorite: Boolean

    @get:PluralsRes
    protected abstract val pluralRes: Int

    @Composable
    fun labelWithCount(count: Int) = pluralStringResource(pluralRes, count, count)
}
