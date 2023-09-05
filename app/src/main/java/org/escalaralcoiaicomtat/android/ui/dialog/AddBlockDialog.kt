package org.escalaralcoiaicomtat.android.ui.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.type.BlockingRecurrenceYearly
import org.escalaralcoiaicomtat.android.storage.type.BlockingTypes
import org.escalaralcoiaicomtat.android.ui.form.FormDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.utils.isDayMonthPossible
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddBlockDialog(
    pathId: Long,
    blocking: Blocking?,
    onCreationRequest: (Blocking) -> Unit,
    onDeleteRequest: (Blocking) -> Unit,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState { 3 }
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage = it }
    }
    LaunchedEffect(blocking) {
        if (blocking?.recurrence != null) {
            pagerState.scrollToPage(1)
        } else if (blocking?.endDate != null) {
            pagerState.scrollToPage(2)
        }
    }

    var type: BlockingTypes? by remember { mutableStateOf(blocking?.type) }

    var fromDay: String? by remember { mutableStateOf(blocking?.recurrence?.fromDay?.toString()) }
    var fromMonth: Month? by remember { mutableStateOf(blocking?.recurrence?.fromMonth) }
    var toDay: String? by remember { mutableStateOf(blocking?.recurrence?.toDay?.toString()) }
    var toMonth: Month? by remember { mutableStateOf(blocking?.recurrence?.toMonth) }

    var endDate: LocalDate? by remember { mutableStateOf(blocking?.endDate?.toLocalDate()) }

    var isPickingDate by remember { mutableStateOf(false) }
    if (isPickingDate) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { isPickingDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDate = Instant
                            .ofEpochMilli(pickerState.selectedDateMillis!!)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        isPickingDate = false
                    },
                    enabled = pickerState.selectedDateMillis != null
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isPickingDate = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.block_new_title)) },
        text = {
            Column {
                if (blocking != null) {
                    OutlinedButton(
                        onClick = { onDeleteRequest(blocking) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                }

                FormDropdown(
                    selection = type,
                    onSelectionChanged = { type = it },
                    options = BlockingTypes.entries,
                    label = stringResource(R.string.block_new_type),
                    toString = { stringResource(it.titleRes) },
                    icon = { it.iconRes },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.block_new_mode_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        label = { Text(stringResource(R.string.block_new_mode_forever)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    FilterChip(
                        selected = currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        label = { Text(stringResource(R.string.block_new_mode_recurrence)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                    FilterChip(
                        selected = currentPage == 2,
                        onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                        label = { Text(stringResource(R.string.block_new_mode_date)) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> { /* Forever doesn't display anything */
                        }

                        1 -> Column(modifier = Modifier.fillMaxWidth()) {
                            // Recurrence
                            Text(
                                text = stringResource(R.string.block_new_mode_recurrence_title),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )

                            Text(
                                text = stringResource(R.string.block_new_mode_recurrence_from),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                FormField(
                                    value = fromDay,
                                    onValueChange = { fromDay = it },
                                    label = stringResource(R.string.form_day),
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                FormDropdown(
                                    selection = fromMonth,
                                    onSelectionChanged = { fromMonth = it },
                                    options = Month.values().toList(),
                                    label = stringResource(R.string.form_month),
                                    modifier = Modifier.weight(2f),
                                    toString = {
                                        it.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        )
                                    }
                                )
                            }
                            Text(
                                text = stringResource(R.string.block_new_mode_recurrence_until),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                FormField(
                                    value = toDay,
                                    onValueChange = { toDay = it },
                                    label = stringResource(R.string.form_day),
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )
                                FormDropdown(
                                    selection = toMonth,
                                    onSelectionChanged = { toMonth = it },
                                    options = Month.values().toList(),
                                    label = stringResource(R.string.form_month),
                                    modifier = Modifier.weight(2f),
                                    toString = {
                                        it.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        )
                                    }
                                )
                            }
                        }

                        2 -> FormField(
                            value = endDate?.let(DateTimeFormatter.ISO_DATE::format),
                            onValueChange = {},
                            label = stringResource(R.string.block_new_mode_end_date),
                            readOnly = true,
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is PressInteraction.Release) {
                                                isPickingDate = true
                                            }
                                        }
                                    }
                                },
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.CalendarMonth,
                                    stringResource(R.string.block_new_mode_end_date)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreationRequest(
                        Blocking(
                            blocking?.id ?: 0L,
                            Instant.now(),
                            type!!,
                            if (currentPage == 1)
                                BlockingRecurrenceYearly(
                                    fromDay!!.toUShort(),
                                    fromMonth!!,
                                    toDay!!.toUShort(),
                                    toMonth!!
                                )
                            else
                                null,
                            if (currentPage == 2)
                                endDate?.atStartOfDay(ZoneId.systemDefault())
                            else
                                null,
                            pathId = pathId
                        )
                    )
                },
                enabled = type != null && when (currentPage) {
                    // Recurrence
                    1 -> isDayMonthPossible(fromDay, fromMonth) &&
                        isDayMonthPossible(toDay, toMonth)
                    // End date
                    2 -> endDate != null
                    else -> true
                }
            ) {
                Text(
                    text = stringResource(
                        if (blocking != null)
                            R.string.action_update
                        else
                            R.string.action_create
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(widthDp = 500)
@Composable
fun AddBlockDialog_Preview() {
    AddBlockDialog(
        pathId = 0,
        blocking = null,
        onCreationRequest = {},
        onDeleteRequest = {},
        onDismissRequest = {}
    )
}
