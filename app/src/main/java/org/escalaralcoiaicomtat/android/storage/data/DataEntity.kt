package org.escalaralcoiaicomtat.android.storage.data

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource

abstract class DataEntity : BaseEntity() {
    abstract val displayName: String
    abstract val isFavorite: Boolean

    @get:PluralsRes
    protected abstract val pluralRes: Int

    @get:StringRes
    abstract val childrenTitleRes: Int

    @Composable
    fun labelWithCount(count: Int) = pluralStringResource(pluralRes, count, count)
}
