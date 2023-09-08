package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.icons.Sunlight
import org.escalaralcoiaicomtat.android.ui.icons.Sunrise
import org.escalaralcoiaicomtat.android.ui.icons.Sunset

/**
 * Represents different times of the day.
 */
enum class SunTime(
    @StringRes val label: Int,
    @StringRes val title: Int,
    @StringRes val message: Int,
    val icon: ImageVector
) {
    None(
        R.string.sun_time_none,
        R.string.sun_time_none_title,
        R.string.sun_time_none_message,
        Icons.Rounded.Sunlight
    ),
    Morning(
        R.string.sun_time_morning,
        R.string.sun_time_morning_title,
        R.string.sun_time_morning_message,
        Icons.Rounded.Sunrise
    ),
    Afternoon(
        R.string.sun_time_afternoon,
        R.string.sun_time_afternoon_title,
        R.string.sun_time_afternoon_message,
        Icons.Rounded.Sunset
    ),
    Day(
        R.string.sun_time_day,
        R.string.sun_time_day_title,
        R.string.sun_time_day_message,
        Icons.Rounded.WbSunny
    )
}
