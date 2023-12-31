package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

data class SafesCount(
    val amount: Long,
    @PluralsRes val pluralString: Int,
    @StringRes val uncountableString: Int
) {
    companion object {
        /**
         * From this count upwards, the amount will be considered uncountable, and simply that it's not
         * known how many safes there are.
         */
        const val MANY_SAFES = 255
    }

    val text: String
        @Composable
        get() = if (amount >= MANY_SAFES)
            stringResource(uncountableString)
        else
            pluralStringResource(pluralString, amount.toInt(), amount)
}
