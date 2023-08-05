package org.escalaralcoiaicomtat.android.ui.reusable.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationBar(
    currentPage: Int,
    items: List<NavigationItem?>,
    alwaysShowLabel: Boolean,
    onItemSelected: suspend (index: Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    androidx.compose.material3.NavigationBar {
        items.filterNotNull().forEachIndexed { index, item ->
            NavigationBarItem(
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
