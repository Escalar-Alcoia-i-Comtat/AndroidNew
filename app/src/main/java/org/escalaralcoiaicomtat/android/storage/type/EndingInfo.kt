package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

enum class EndingInfo(@StringRes val displayName: Int) {
    RAPPEL(R.string.ending_info_rappel),
    EQUIPPED(R.string.ending_info_equipped),
    CLEAN(R.string.ending_info_clean)
}
