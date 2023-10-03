package org.escalaralcoiaicomtat.android.ui.dialog

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.core.os.LocaleListCompat
import org.escalaralcoiaicomtat.android.R
import java.util.Locale

@Composable
fun LanguageDialog(
    language: Locale?,
    onLanguageChanged: (Locale?) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            LazyColumn {
                items(
                    listOf(
                        null,
                        Locale.ENGLISH,
                        Locale.forLanguageTag("ca"),
                        Locale.forLanguageTag("es")
                    )
                ) { locale ->
                    val localeDisplayName = locale?.let {
                        locale.getDisplayName(locale)
                            .capitalize(androidx.compose.ui.text.intl.Locale.current)
                    } ?: stringResource(R.string.settings_language_default)

                    ListItem(
                        leadingContent = {
                            Icon(
                                imageVector = if (locale == language)
                                    Icons.Rounded.RadioButtonChecked
                                else
                                    Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = localeDisplayName
                            )
                        },
                        headlineContent = { Text(localeDisplayName) },
                        modifier = Modifier.clickable {
                            val newLocale = if (locale == null) {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.getDefault()
                                )
                                null
                            } else {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.create(locale)
                                )
                                locale
                            }
                            onLanguageChanged(newLocale)
                            onDismissRequest()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
