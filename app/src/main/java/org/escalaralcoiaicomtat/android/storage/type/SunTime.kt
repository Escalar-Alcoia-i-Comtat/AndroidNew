package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

/**
 * Represents different times of the day.
 */
enum class SunTime (@StringRes val label: Int) {
    None(R.string.sun_time_none),
    Morning(R.string.sun_time_morning),
    Afternoon(R.string.sun_time_afternoon),
    Day(R.string.sun_time_day)
}
