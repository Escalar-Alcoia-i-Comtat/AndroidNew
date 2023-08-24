package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

object DropdownChip {
    data class Option(
        val text: @Composable () -> String,
        val highlighted: Boolean = false
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DropdownChip(
    label: String,
    options: List<DropdownChip.Option>,
    onSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        FilterChip(
            selected = active,
            onClick = { expanded = true },
            label = { Text(label) },
            modifier = modifier.menuAnchor()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option.text()) },
                    onClick = { onSelected(index) },
                    colors = if (option.highlighted)
                        MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    else
                        MenuDefaults.itemColors()
                )
            }
        }
    }
}
