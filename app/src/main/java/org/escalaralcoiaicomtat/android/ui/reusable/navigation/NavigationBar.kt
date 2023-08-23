package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationBar(
    currentRoute: String?,
    items: List<NavigationItem?>,
    alwaysShowLabel: Boolean,
    onItemSelected: suspend (NavigationItem) -> Unit
) {
    val scope = rememberCoroutineScope()

    androidx.compose.material3.NavigationBar {
        items.filterNotNull().forEach { item ->
            NavigationBarItem(
                selected = item.route == currentRoute,
                onClick = { scope.launch { onItemSelected(item) } },
                icon = {
                    Icon(
                        imageVector = if (item.route == currentRoute) item.activeIcon else item.defaultIcon,
                        contentDescription = item.label()
                    )
                },
                label = { Text(item.label()) },
                alwaysShowLabel = alwaysShowLabel
            )
        }
    }
}
