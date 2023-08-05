package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a navigation item used in a navigation menu or navigation bar.
 *
 * @property label The label of the navigation item, displayed as a composable function.
 * @property activeIcon The active icon for the navigation item, represented as an ImageVector.
 * @property defaultIcon The default icon for the navigation item, represented as an ImageVector.
 */
data class NavigationItem(
    val label: @Composable () -> String,
    val activeIcon: ImageVector,
    val defaultIcon: ImageVector
)
