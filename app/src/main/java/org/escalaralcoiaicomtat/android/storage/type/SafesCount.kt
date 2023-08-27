package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource

data class SafesCount(
    val amount: Long,
    @PluralsRes val displayName: Int
) {
    val text: String
        @Composable
        get() = pluralStringResource(displayName, amount.toInt(), amount)
}
