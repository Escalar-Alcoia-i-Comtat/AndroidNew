package org.escalaralcoiaicomtat.android.unit

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToLong

data class DistanceUnit(
    val meters: Double,
    val spec: DistanceUnitSpec
) {
    constructor(meters: Double) : this(
        meters,
        DistanceUnitSpec.Meters
    )

    val value: Double = meters * spec.metersRatio

    override fun toString(): String = "$meters m"

    @Composable
    fun decimalLabel(): String = stringResource(spec.decimal, value.roundToLong())

    @Composable
    fun floatLabel(): String = stringResource(spec.float, value.roundToLong())

    @Composable
    fun tag(): String = stringResource(spec.tag)

    operator fun times(i: Int): DistanceUnit = copy(meters = meters * i)

    fun toFeet(): DistanceUnit = copy(
        meters = meters / spec.metersRatio,
        spec = DistanceUnitSpec.Feet
    )
}
