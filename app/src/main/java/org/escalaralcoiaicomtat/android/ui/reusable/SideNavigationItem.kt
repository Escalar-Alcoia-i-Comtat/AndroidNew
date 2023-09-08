package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.escalaralcoiaicomtat.android.R

@Composable
fun SideNavigationItem(
    label: String,
    @IntRange(from = 0, to = 2) depth: Int,
    selected: Boolean,
    isEditable: Boolean,
    onClick: () -> Unit,
    onCreate: () -> Unit,
    onEdit: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = when(depth) {
                    0 -> MaterialTheme.typography.labelLarge
                    1 -> MaterialTheme.typography.labelMedium
                    else -> MaterialTheme.typography.labelSmall
                }
            )
        },
        selected = selected,
        onClick = onClick.takeIf { !selected } ?: {},
        badge = {
            if (isEditable) {
                Row {
                    IconButton(onClick = onCreate) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.action_create)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit)
                        )
                    }
                }
            }
        },
        modifier = Modifier.padding(start = 8.dp * depth)
    )
}
