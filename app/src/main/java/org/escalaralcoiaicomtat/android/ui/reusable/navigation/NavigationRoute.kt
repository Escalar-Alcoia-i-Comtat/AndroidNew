package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavType

/**
 * Represents a route that can be navigated to in the app.
 *
 * @property route The route to be used as an identifier for the item's location in navigation.
 */
open class NavigationRoute(
    val route: String,
    val arguments: List<Argument<*>> = emptyList(),
    val root: String = route
) {

    data class Argument<T>(
        val name: String,
        val navType: NavType<T>,
        val nullable: Boolean = false
    )

    override fun equals(other: Any?): Boolean {
        if (other is NavDestination) return other.route == route
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigationRoute

        if (route != other.route) return false

        return true
    }

    override fun hashCode(): Int {
        return route.hashCode()
    }
}
