package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.compose.runtime.Composable

/**
 * Provides an indicator for showing the user the progress of a task.
 * @param progress If null, the indicator will be indeterminate. Otherwise, the current progress
 * will be a proportion of [Pair.second] and [Pair.first].
 */
@Composable
fun CircularProgressIndicator(progress: Pair<Int, Int>? = null) {
    val value = progress?.let { (current, max) -> current.toFloat() / max }

    if (value != null && !value.isNaN()) {
        androidx.compose.material3.CircularProgressIndicator(value)
    } else {
        androidx.compose.material3.CircularProgressIndicator()
    }
}
