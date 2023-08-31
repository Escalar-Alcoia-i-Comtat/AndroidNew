package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormDropdown(
    selection: T?,
    onSelectionChanged: (T) -> Unit,
    options: List<T>,
    label: String?,
    modifier: Modifier = Modifier,
    toString: @Composable (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    val foregroundColor = if (expanded)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selection?.let { toString(it) } ?: "",
            onValueChange = { },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = foregroundColor,
                disabledContainerColor = MaterialTheme.colorScheme.background,
                disabledLabelColor = foregroundColor,
                disabledLeadingIconColor = foregroundColor,
                disabledPlaceholderColor = foregroundColor.copy(alpha = .5f),
                disabledPrefixColor = foregroundColor,
                disabledSuffixColor = foregroundColor,
                disabledSupportingTextColor = foregroundColor,
                disabledTextColor = foregroundColor,
                disabledTrailingIconColor = foregroundColor,
            ),
            label = { if (label != null) Text(label) },
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                expanded = !expanded
                            }
                        }
                    }
                },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = modifier.menuAnchor(),
            maxLines = 1,
            singleLine = true
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(toString(option)) },
                    onClick = {
                        onSelectionChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
