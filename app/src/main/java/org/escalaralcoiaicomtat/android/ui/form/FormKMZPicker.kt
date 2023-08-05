package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.escalaralcoiaicomtat.android.R

@Composable
fun FormKMZPicker(
    fileName: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = fileName ?: stringResource(R.string.form_kmz_pick),
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
                            onClick()
                        }
                    }
                }
            },
        leadingIcon = {
            Icon(Icons.Rounded.FileOpen, stringResource(R.string.form_kmz_pick))
        }
    )
}
