package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import org.escalaralcoiaicomtat.android.utils.letIfNotNull

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String?,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    valueAssertion: ValueAssertion = ValueAssertion.NONE,
    thisFocusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    onGo: (() -> Unit)? = null,
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val isError = if (value == null) {
        false
    } else {
        when (valueAssertion) {
            ValueAssertion.NONE -> false
            ValueAssertion.NUMBER -> value.isBlank() || !value.isDigitsOnly()
            ValueAssertion.NUMBER_OR_EMPTY -> !value.isDigitsOnly()
        }
    }

    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        label = { if (label != null) Text(label) },
        modifier = modifier
            .letIfNotNull(thisFocusRequester, Modifier::focusRequester),
        keyboardOptions = KeyboardOptions(
            capitalization = keyboardCapitalization,
            autoCorrect = true,
            keyboardType = keyboardType,
            imeAction = when {
                nextFocusRequester != null -> ImeAction.Next
                onGo != null -> ImeAction.Go
                else -> ImeAction.Done
            },
        ),
        keyboardActions = KeyboardActions {
            nextFocusRequester?.requestFocus()
                ?: onGo?.invoke()
                ?: softwareKeyboardController?.hide()
        },
        singleLine = maxLines <= 1,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        leadingIcon = leadingContent,
        trailingIcon = trailingContent,
        isError = isError,
        supportingText = {
            if (isError && valueAssertion.errorString != null) {
                Text(text = stringResource(valueAssertion.errorString))
            } else if (supportingText != null) {
                Text(text = supportingText)
            }
        }
    )
}
