package org.escalaralcoiaicomtat.android.ui.reusable.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import org.escalaralcoiaicomtat.android.R

data class ToolbarAction(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String? = label,
    val onClick: () -> Unit
)

/**
 * To be used inside the `actions` parameter of a Toolbar. Shows as many actions as allowed by
 * [maxItems], if there are more actions than this amount, they are hidden inside an overflow menu.
 *
 * @param actions The action buttons to display.
 * @param maxItems The maximum amount of icons to display, including overflow.
 */
@Composable
fun RowScope.ToolbarActionsOverflow(actions: List<ToolbarAction>, maxItems: Int = 3) {
    if (actions.isEmpty()) return

    val actionsToDisplay = actions.subList(0, minOf(actions.size, maxItems))
    for (action in actionsToDisplay) {
        IconButton(onClick = action.onClick) {
            Icon(action.icon, action.contentDescription)
        }
    }

    // If no overflow is needed, return
    if (actions.size <= maxItems) return

    var isExpanded by remember { mutableStateOf(false) }

    val overflowActions = actions.subList(maxItems, actions.size)
    IconButton(onClick = { isExpanded = true }) {
        Icon(Icons.Rounded.MoreVert, stringResource(R.string.action_more))
    }
    DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
        for (action in overflowActions) {
            DropdownMenuItem(
                text = { Text(action.label) },
                onClick = action.onClick
            )
        }
    }
}
