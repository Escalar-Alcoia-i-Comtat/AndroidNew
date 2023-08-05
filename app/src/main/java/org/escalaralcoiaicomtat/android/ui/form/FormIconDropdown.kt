package org.escalaralcoiaicomtat.android.ui.form

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

data class PointOption(
    val key: String,
    val icon: ImageVector,
    @StringRes val label: Int
)

@Composable
fun FormIconDropdown(
    selection: PointOption?,
    onSelectionChanged: (PointOption) -> Unit,
    options: List<PointOption>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
    ) {
        IconButton(
            onClick = { expanded = !expanded }
        ) {
            selection?.let { Icon(it.icon, it.key) }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(option.icon, option.key)
                            Text(stringResource(option.label))
                        }
                    },
                    onClick = {
                        onSelectionChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
