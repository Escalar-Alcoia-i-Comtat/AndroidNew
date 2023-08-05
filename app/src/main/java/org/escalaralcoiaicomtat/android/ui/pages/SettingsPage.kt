package org.escalaralcoiaicomtat.android.ui.pages

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.ui.dialog.ApiKeyDialog
import org.escalaralcoiaicomtat.android.ui.dialog.LanguageDialog
import java.util.Locale

@Composable
fun SettingsPage(
    onApiKeySubmit: (key: String) -> Job
) {
    val context = LocalContext.current

    Text(
        text = stringResource(R.string.settings_category_ui),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 22.sp
    )

    var language: Locale? by remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales().get(0))
    }

    var isShowingLanguageDialog by remember { mutableStateOf(false) }
    if (isShowingLanguageDialog) {
        LanguageDialog(language, { language = it }) { isShowingLanguageDialog = false }
    }

    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.Language, stringResource(R.string.settings_language))
        },
        headlineContent = { Text(stringResource(R.string.settings_language)) },
        supportingContent = {
            val languageDisplayName = language?.let { it.getDisplayName(it) }
            Text(
                text = languageDisplayName ?: stringResource(R.string.settings_language_default)
            )
        },
        modifier = Modifier.clickable { isShowingLanguageDialog = true }
    )

    Text(
        text = stringResource(R.string.settings_category_security),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 22.sp
    )


    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)
    var showingApiKeyDialog by remember { mutableStateOf(false) }
    if (showingApiKeyDialog) {
        ApiKeyDialog(onApiSubmit = onApiKeySubmit) {
            showingApiKeyDialog = false
        }
    }

    ListItem(
        leadingContent = {
            Icon(
                if (apiKey == null)
                    Icons.Outlined.Lock
                else
                    Icons.Outlined.LockOpen,
                stringResource(R.string.settings_security_lock)
            )
        },
        headlineContent = { Text(stringResource(R.string.settings_security_lock)) },
        supportingContent = {
            val text = stringResource(
                if (apiKey == null)
                    R.string.settings_security_lock_locked
                else
                    R.string.settings_security_lock_unlocked
            )
            Text(text)
        },
        modifier = Modifier.clickable { showingApiKeyDialog = true }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsPage_Preview() {
    SettingsPage(
        onApiKeySubmit = { CoroutineScope(Dispatchers.IO).launch { } }
    )
}
