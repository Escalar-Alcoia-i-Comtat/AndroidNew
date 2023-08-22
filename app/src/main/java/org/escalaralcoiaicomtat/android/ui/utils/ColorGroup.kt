package org.escalaralcoiaicomtat.android.ui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ColorGroup(
    val light: Color,
    val onLight: Color,
    val dark: Color,
    val onDark: Color
) {
    constructor(colors: LightDarkColor, onColors: LightDarkColor) :
        this(colors.light, onColors.light, colors.dark, onColors.dark)

    val color: Color
        @Composable
        get() = if (isSystemInDarkTheme())
            dark
        else
            light

    val onColor: Color
        @Composable
        get() = if (isSystemInDarkTheme())
            onDark
        else
            onLight
}

data class LightDarkColor(
    val light: Color,
    val dark: Color
)

infix fun Color.orDark(dark: Color) = LightDarkColor(this, dark)

infix fun LightDarkColor.under(onColors: LightDarkColor) = ColorGroup(this, onColors)
