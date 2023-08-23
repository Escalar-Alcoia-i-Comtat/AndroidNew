package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination

/**
 * Represents a navigation item used in a navigation menu or navigation bar.
 *
 * @property route The route to be used as an identifier for the item's location in navigation.
 * @property label The label of the navigation item, displayed as a composable function.
 * @property activeIcon The active icon for the navigation item, represented as an ImageVector.
 * @property defaultIcon The default icon for the navigation item, represented as an ImageVector.
 */
open class NavigationItem(
    route: String,
    arguments: List<Argument<*>> = emptyList(),
    val label: ILabel,
    val activeIcon: ImageVector,
    val defaultIcon: ImageVector
): NavigationRoute(route, arguments) {
    fun interface ILabel {
        @Composable
        operator fun invoke(): String
    }

    override fun equals(other: Any?): Boolean {
        if (other is NavDestination) return other.route == route
        if (other is NavigationRoute) return other.route == route
        if (javaClass != other?.javaClass) return false

        return false
    }

    override fun hashCode(): Int {
        return route.hashCode()
    }
}
