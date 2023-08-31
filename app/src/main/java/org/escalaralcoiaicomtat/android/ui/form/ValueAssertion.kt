package org.escalaralcoiaicomtat.android.ui.form

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

enum class ValueAssertion(@StringRes val errorString: Int?) {
    NONE(null), NUMBER(R.string.assert_number_fail), NUMBER_OR_EMPTY(R.string.assert_number_fail)
}
