package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormDropdown(
    selection: T?,
    onSelectionChanged: (T) -> Unit,
    options: List<T>,
    label: String?,
    modifier: Modifier = Modifier,
    icon: (@Composable (T) -> ImageVector)? = null,
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
            label = label?.let { { Text(it) } },
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
                    text = {
                        Text(
                            text = toString(option),
                            color = if (selection == option)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = icon?.invoke(option)?.let {
                        { Icon(it, toString(option)) }
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
