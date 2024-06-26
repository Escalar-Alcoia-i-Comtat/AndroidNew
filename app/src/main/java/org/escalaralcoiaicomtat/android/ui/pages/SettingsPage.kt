package org.escalaralcoiaicomtat.android.ui.pages

import android.text.format.Formatter
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.BuildConfig
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.files.FilesCrate
import org.escalaralcoiaicomtat.android.ui.dialog.ApiKeyDialog
import org.escalaralcoiaicomtat.android.ui.dialog.LanguageDialog
import org.escalaralcoiaicomtat.android.utils.launchUrl
import org.escalaralcoiaicomtat.android.utils.toast
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsPage(
    onApiKeySubmit: (key: String) -> Job
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val crate = FilesCrate.rememberInstance()

    if (BuildConfig.DEBUG) {
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Feedback,
                    contentDescription = stringResource(R.string.settings_warning_debug_title),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.settings_warning_debug_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.settings_warning_debug_message_1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(
                            R.string.settings_warning_debug_message_2,
                            BuildConfig.HOSTNAME
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

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
        text = stringResource(R.string.settings_category_storage),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 22.sp
    )

    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.TextFields, stringResource(R.string.settings_storage_cache_title))
        },
        headlineContent = { Text(stringResource(R.string.settings_storage_cache_title)) },
        supportingContent = {
            val size by crate.cacheSizeLive().observeAsState()

            Text(
                stringResource(
                    R.string.settings_storage_cache_description,
                    size?.let {
                        Formatter.formatShortFileSize(context, it)
                    } ?: stringResource(R.string.state_calculating)
                )
            )
        },
        modifier = Modifier.clickable {
            crate.cacheClear().invokeOnCompletion {
                context.toast(R.string.settings_storage_cache_cleared)
            }
        }
    )

    val liveSync by SyncWorker.getLive(context).observeAsState(initial = emptyList())
    val sync = liveSync.find { it.state == WorkInfo.State.RUNNING }
    val lastSync by Preferences.getLastSync(context).collectAsState(initial = null)

    fun scheduleSync(force: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            SyncWorker.synchronize(context, force).observe(lifecycleOwner) { info: WorkInfo? ->
                if (info == null) return@observe
                Timber.i("SyncWorker: %s", info.state.name)
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    Timber.i("SyncWorker: %s", info.outputData.keyValueMap)
                    info.outputData.getString(SyncWorker.RESULT_STOP_REASON)?.let {
                        val stopReason = SyncWorker.StopReason.valueOf(it)
                        if (stopReason == SyncWorker.StopReason.ALREADY_UP_TO_DATE) {
                            context.toast(R.string.toast_up_to_date)
                        }
                    }
                }
            }
        }
    }

    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.CloudSync, stringResource(R.string.settings_storage_sync_title))
        },
        headlineContent = { Text(stringResource(R.string.settings_storage_sync_title)) },
        supportingContent = {
            Text(
                text = if (sync != null)
                    stringResource(R.string.settings_storage_sync_description_running)
                else
                    stringResource(
                        R.string.settings_storage_sync_description,
                        lastSync?.let {
                            DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
                                .format(it.atZone(ZoneId.systemDefault()))
                        } ?: stringResource(R.string.none)
                    )
            )
        },
        modifier = Modifier.combinedClickable(
            enabled = sync == null,
            onLongClick = { scheduleSync(true) },
            onClick = { scheduleSync(false) }
        )
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


    Text(
        text = stringResource(R.string.settings_category_advanced),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 22.sp
    )

    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.TextFields, stringResource(R.string.settings_info_version))
        },
        headlineContent = { Text(stringResource(R.string.settings_info_version)) },
        supportingContent = { Text(BuildConfig.VERSION_NAME) },
        modifier = Modifier.clickable {
            clipboardManager.setText(
                buildAnnotatedString { append(BuildConfig.VERSION_NAME) }
            )
        }
    )
    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.Numbers, stringResource(R.string.settings_info_build))
        },
        headlineContent = { Text(stringResource(R.string.settings_info_build)) },
        supportingContent = { Text(BuildConfig.VERSION_CODE.toString()) },
        modifier = Modifier.clickable {
            clipboardManager.setText(
                buildAnnotatedString { append(BuildConfig.VERSION_CODE.toString()) }
            )
        }
    )
    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.Code, stringResource(R.string.settings_info_source_code_title))
        },
        headlineContent = { Text(stringResource(R.string.settings_info_source_code_title)) },
        supportingContent = { Text(stringResource(R.string.settings_info_source_code_message)) },
        modifier = Modifier.clickable {
            context.launchUrl("https://github.com/Escalar-Alcoia-i-Comtat/AndroidNew")
        }
    )
    ListItem(
        leadingContent = {
            Icon(Icons.Outlined.Language, stringResource(R.string.settings_info_translations_title))
        },
        headlineContent = { Text(stringResource(R.string.settings_info_translations_title)) },
        supportingContent = { Text(stringResource(R.string.settings_info_translations_message)) },
        modifier = Modifier.clickable {
            context.launchUrl("https://crowdin.com/project/escalar-alcoia-i-comtat")
        }
    )

    if (!BuildConfig.DEBUG) {
        val hasOptedInForErrorCollection by Preferences.hasOptedInForErrorCollection(context)
            .collectAsState(initial = true)
        var optedInForErrorCollectionEnabled by remember { mutableStateOf(true) }
        val hasOptedInForPerformanceMetrics by Preferences.hasOptedInForPerformanceMetrics(context)
            .collectAsState(initial = true)
        var optedInForPerformanceMetricsEnabled by remember { mutableStateOf(true) }
        ListItem(
            leadingContent = {
                Icon(Icons.Outlined.BugReport, stringResource(R.string.settings_info_error_collection_title))
            },
            headlineContent = { Text(stringResource(R.string.settings_info_error_collection_title)) },
            supportingContent = { Text(stringResource(R.string.settings_info_error_collection_message)) },
            modifier = Modifier.clickable(optedInForErrorCollectionEnabled) {
                optedInForErrorCollectionEnabled = false
                CoroutineScope(Dispatchers.IO).async {
                    Preferences.optInForErrorCollection(context, !hasOptedInForErrorCollection)
                }.invokeOnCompletion { optedInForErrorCollectionEnabled = true }
            },
            trailingContent = {
                Switch(
                    checked = hasOptedInForErrorCollection,
                    onCheckedChange = {
                        optedInForErrorCollectionEnabled = false
                        CoroutineScope(Dispatchers.IO).async {
                            Preferences.optInForErrorCollection(context, it)
                        }.invokeOnCompletion { optedInForErrorCollectionEnabled = true }
                    },
                    enabled = optedInForErrorCollectionEnabled
                )
            }
        )
        ListItem(
            leadingContent = {
                Icon(Icons.Outlined.Bolt, stringResource(R.string.settings_info_performance_metrics_title))
            },
            headlineContent = { Text(stringResource(R.string.settings_info_performance_metrics_title)) },
            supportingContent = { Text(stringResource(R.string.settings_info_performance_metrics_message)) },
            modifier = Modifier.clickable(hasOptedInForErrorCollection && optedInForPerformanceMetricsEnabled) {
                optedInForPerformanceMetricsEnabled = false
                CoroutineScope(Dispatchers.IO).async {
                    Preferences.optInForPerformanceMetrics(context, !hasOptedInForPerformanceMetrics)
                }.invokeOnCompletion { optedInForPerformanceMetricsEnabled = true }
            },
            trailingContent = {
                Switch(
                    checked = hasOptedInForErrorCollection && hasOptedInForPerformanceMetrics,
                    onCheckedChange = {
                        optedInForPerformanceMetricsEnabled = false
                        CoroutineScope(Dispatchers.IO).async {
                            Preferences.optInForPerformanceMetrics(context, it)
                        }.invokeOnCompletion { optedInForPerformanceMetricsEnabled = true }
                    },
                    enabled = hasOptedInForErrorCollection && optedInForPerformanceMetricsEnabled
                )
            }
        )

        val deviceId by Preferences.getDeviceId(context).collectAsState(initial = null)
        Text(
            text = deviceId ?: "",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPage_Preview() {
    SettingsPage(
        onApiKeySubmit = { CoroutineScope(Dispatchers.IO).launch { } }
    )
}
