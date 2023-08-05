package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.compose.runtime.Composable

/**
 * Provides an indicator for showing the user the progress of a task.
 * @param progress If null, the indicator will be indeterminate. Otherwise, the current progress
 * will be a proportion of [Pair.second] and [Pair.first].
 */
@Composable
fun CircularProgressIndicator(progress: Pair<Int, Int>? = null) {
    if (progress != null) {
        val (current, max) = progress
        val value = current.toFloat() / max
        androidx.compose.material3.CircularProgressIndicator(value)
    } else {
        androidx.compose.material3.CircularProgressIndicator()
    }
}
