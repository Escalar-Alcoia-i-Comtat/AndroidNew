package org.escalaralcoiaicomtat.android.unit

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

/**
 * Distance unit specification.
 * @param metersRatio The ratio of the unit to meters.
 * @param decimal The string resource for the decimal label.
 * @param float The string resource for the float label.
 * @param tag The string resource for the tag label.
 */
sealed class DistanceUnitSpec(
    val metersRatio: Double,
    @StringRes val decimal: Int,
    @StringRes val float: Int,
    @StringRes val tag: Int
) {
    data object Meters : DistanceUnitSpec(
        1.0,
        R.string.distance_unit_meters_decimal,
        R.string.distance_unit_meters_float,
        R.string.distance_unit_meters_tag
    )

    data object Feet : DistanceUnitSpec(
        3.28084,
        R.string.distance_unit_feet_decimal,
        R.string.distance_unit_feet_float,
        R.string.distance_unit_feet_tag
    )
}
