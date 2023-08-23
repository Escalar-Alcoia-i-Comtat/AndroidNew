package org.escalaralcoiaicomtat.android.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

fun Modifier.backAnimation(progress: Float?) =
    negativePadding(
        start = if (progress != null) (64 * progress).toInt() else 0,
        end = if (progress != null) (-64 * progress).toInt() else 0
    ).alpha(progress?.let { 1 - it } ?: 1f)
