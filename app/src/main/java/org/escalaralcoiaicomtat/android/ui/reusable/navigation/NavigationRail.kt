package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun NavigationRail(
    currentPage: Int,
    items: List<NavigationItem?>,
    modifier: Modifier = Modifier,
    alwaysShowLabel: Boolean = false,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets,
    onItemSelected: suspend (index: Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    androidx.compose.material3.NavigationRail(
        modifier = modifier,
        header = header,
        windowInsets = windowInsets
    ) {
        items.filterNotNull().forEachIndexed { index, item ->
            NavigationRailItem(
                selected = index == currentPage,
                onClick = { scope.launch { onItemSelected(index) } },
                icon = {
                    Icon(
                        imageVector = if (index == currentPage) item.activeIcon else item.defaultIcon,
                        contentDescription = item.label()
                    )
                },
                label = { Text(item.label()) },
                alwaysShowLabel = alwaysShowLabel
            )
        }
    }
}
