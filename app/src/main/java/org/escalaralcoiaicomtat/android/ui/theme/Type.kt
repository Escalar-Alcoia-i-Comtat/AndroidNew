package org.escalaralcoiaicomtat.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = jost),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = jost),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = jost),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = jost),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = jost),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = jost),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = roboto),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = roboto),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = roboto),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = rubik, fontSize = 18.sp, fontWeight = FontWeight.Bold),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = rubik, fontSize = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = rubik, fontSize = 14.sp, fontWeight = FontWeight.Medium)

)