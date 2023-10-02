package org.escalaralcoiaicomtat.android.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.ui.modifier.autofill

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ApiKeyDialog(
    onApiSubmit: (String) -> Job,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    var apiKey by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest.takeIf { isChecking } ?: {},
        title = { Text(stringResource(R.string.settings_security_lock)) },
        text = {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; isError = false },
                label = { Text(stringResource(R.string.settings_security_lock_api_key)) },
                isError = isError,
                supportingText = {
                    AnimatedVisibility(visible = isError) {
                        Text(stringResource(R.string.settings_security_lock_api_key_wrong))
                    }
                },
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .autofill(
                        autofillTypes = listOf(AutofillType.Password),
                        onFill = { apiKey = it }
                    )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isChecking = true
                    onApiSubmit(apiKey).invokeOnCompletion {
                        isChecking = false

                        CoroutineScope(Dispatchers.IO).launch {
                            val currentApiKey = Preferences.getApiKey(context).firstOrNull()
                            if (currentApiKey == null) {
                                // There was an error
                                isError = true
                            } else {
                                // key was correct, dismiss
                                onDismissRequest()
                            }
                        }
                    }
                },
                enabled = apiKey.isNotBlank() && !isChecking
            ) {
                Text(text = stringResource(R.string.settings_security_lock_unlock))
            }
        }
    )
}
