package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

enum class EndingInclination(@StringRes val displayName: Int) {
    VERTICAL(R.string.ending_inclination_vertical),
    DIAGONAL(R.string.ending_inclination_diagonal),
    HORIZONTAL(R.string.ending_inclination_horizontal)
}
