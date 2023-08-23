package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Creates an Scaffold with support with navigation.
 *
 * @throws NoSuchElementException If [items] is empty.
 */
@Composable
fun NavigationScaffold(
    items: List<NavigationRoute>,
    widthSizeClass: WindowWidthSizeClass,
    initialRoute: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    alwaysShowLabel: Boolean = false,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    topBar: @Composable () -> Unit = { },
    snackbarHost: @Composable () -> Unit = { },
    floatingActionButton: @Composable () -> Unit = { },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    pageContentModifier: Modifier = Modifier,
    pageContentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    pageContent: @Composable ColumnScope.(NavigationItem, NavBackStackEntry) -> Unit
) {
    fun navigate(item: NavigationItem) {
        navController.navigate(item.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // re-selecting the same item
            launchSingleTop = true
            // Restore state when re-selecting a previously selected item
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        bottomBar = {
            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                NavigationBar(
                    navController.currentDestination?.route,
                    items.filterIsInstance<NavigationItem>(),
                    alwaysShowLabel,
                    ::navigate
                )
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (widthSizeClass != WindowWidthSizeClass.Compact) {
                NavigationRail(
                    currentRoute = navController.currentDestination?.route,
                    items = items.filterIsInstance<NavigationItem>(),
                    alwaysShowLabel = alwaysShowLabel,
                    header = header,
                    onItemSelected = ::navigate
                )
            }

            NavHost(
                navController,
                startDestination = initialRoute,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                for (item in items) {
                    composable(
                        item.route,
                        arguments = item.arguments.map {
                            navArgument(it.name) {
                                type = it.navType
                                nullable = it.nullable
                            }
                        }
                    ) {
                        if (item is NavigationItem) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = pageContentAlignment
                            ) {
                                Column(
                                    modifier = pageContentModifier
                                ) {
                                    pageContent(item, it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
