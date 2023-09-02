package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import io.ktor.client.request.forms.FormBuilder
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.files.LocalFile
import org.escalaralcoiaicomtat.android.storage.files.LocalFile.Companion.file
import org.escalaralcoiaicomtat.android.storage.type.ArtificialGrade
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.EndingInclination
import org.escalaralcoiaicomtat.android.storage.type.EndingInfo
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.storage.type.SafesCount
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.ui.form.FormDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormListCreator
import org.escalaralcoiaicomtat.android.ui.form.ValueAssertion
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class NewPathActivity : EditorActivity<Sector, Path, NewPathActivity.Model>(
    createTitleRes = R.string.new_path_title,
    editTitleRes = R.string.edit_path_title
) {
    object Contract : ResultContract<NewPathActivity>(NewPathActivity::class)

    override val model: Model by viewModels {
        Model.Factory(parentId!!, elementId, ::onBack)
    }

    override val isScrollable: Boolean = false

    override val maxWidth: Int = 1200

    @Composable
    override fun RowScope.SidePanel(parent: Sector?) {
        val sector = parent!!

        val progress by model.sectorImageProgress.observeAsState()
        val image by model.sectorImage.observeAsState()

        image?.let {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clipToBounds()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(this@NewPathActivity)
                        .file(it)
                        .crossfade(true)
                        .build(),
                    contentDescription = sector.displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(rememberZoomState()),
                    contentScale = ContentScale.Inside
                )
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                progress?.let { CircularProgressIndicator(it) } ?: CircularProgressIndicator()
            }
        }
    }

    @Composable
    override fun ColumnScope.Editor(parent: Sector?) {
        val displayName by model.displayName.observeAsState()
        val sketchId by model.sketchId.observeAsState()

        val height by model.height.observeAsState()
        val grade by model.grade.observeAsState()
        val ending by model.ending.observeAsState()

        val pitches = model.pitches

        val stringCount by model.stringCount.observeAsState()
        val paraboltCount by model.paraboltCount.observeAsState()
        val burilCount by model.burilCount.observeAsState()
        val pitonCount by model.pitonCount.observeAsState()
        val spitCount by model.spitCount.observeAsState()
        val tensorCount by model.tensorCount.observeAsState()

        val crackerRequired by model.crackerRequired.observeAsState()
        val friendRequired by model.friendRequired.observeAsState()
        val lanyardRequired by model.lanyardRequired.observeAsState()
        val nailRequired by model.nailRequired.observeAsState()
        val pitonRequired by model.pitonRequired.observeAsState()
        val stapesRequired by model.stapesRequired.observeAsState()

        val reBuilders by model.reBuilders.observeAsState(initial = emptyList())

        val showDescription by model.showDescription.observeAsState(initial = false)

        val sketchIdFocusRequester = remember { FocusRequester() }
        val heightFocusRequester = remember { FocusRequester() }

        FormField(
            value = parent?.displayName,
            onValueChange = { },
            label = stringResource(R.string.form_parent),
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            leadingContent = {
                Icon(Icons.Outlined.SupervisorAccount, stringResource(R.string.form_parent))
            }
        )

        FormField(
            value = displayName,
            onValueChange = { model.displayName.value = it },
            label = stringResource(R.string.form_display_name),
            modifier = Modifier.fillMaxWidth(),
            nextFocusRequester = sketchIdFocusRequester
        )

        FormField(
            value = sketchId,
            onValueChange = { model.sketchId.value = it },
            label = stringResource(R.string.form_sketch_id),
            modifier = Modifier.fillMaxWidth(),
            thisFocusRequester = sketchIdFocusRequester,
            nextFocusRequester = heightFocusRequester,
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER
        )

        FormField(
            value = height,
            onValueChange = { model.height.value = it.takeIf { it.isNotBlank() } },
            label = stringResource(R.string.form_height),
            modifier = Modifier.fillMaxWidth(),
            thisFocusRequester = heightFocusRequester,
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER
        )
        FormDropdown(
            selection = grade,
            onSelectionChanged = {
                if (grade == it) {
                    model.grade.value = null
                } else {
                    model.grade.value = it
                }
            },
            options = SportsGrade.entries,
            label = stringResource(R.string.form_grade),
            modifier = Modifier.fillMaxWidth()
        ) { it.displayName }
        FormDropdown(
            selection = ending,
            onSelectionChanged = {
                if (ending == it) {
                    model.ending.value = null
                } else {
                    model.ending.value = it
                }
            },
            options = Ending.entries,
            label = stringResource(R.string.form_ending),
            modifier = Modifier.fillMaxWidth()
        ) { stringResource(it.displayName) }

        FormField(
            value = stringCount,
            onValueChange = { model.stringCount.value = it.takeIf { it.isNotBlank() } },
            label = stringResource(R.string.form_string_count),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.form_count_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            CountField(paraboltCount, model.paraboltCount::setValue, R.string.safe_type_parabolt)
            CountField(burilCount, model.burilCount::setValue, R.string.safe_type_buril)
            CountField(pitonCount, model.pitonCount::setValue, R.string.safe_type_piton)
            CountField(spitCount, model.spitCount::setValue, R.string.safe_type_spit)
            CountField(tensorCount, model.tensorCount::setValue, R.string.safe_type_tensor)

            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.form_required_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            RequiredField(
                crackerRequired,
                model.crackerRequired::setValue,
                R.string.required_type_cracker
            )
            RequiredField(
                friendRequired,
                model.friendRequired::setValue,
                R.string.required_type_friend
            )
            RequiredField(
                lanyardRequired,
                model.lanyardRequired::setValue,
                R.string.required_type_lanyard
            )
            RequiredField(nailRequired, model.nailRequired::setValue, R.string.required_type_nail)
            RequiredField(
                pitonRequired,
                model.pitonRequired::setValue,
                R.string.required_type_piton
            )
            RequiredField(
                stapesRequired,
                model.stapesRequired::setValue,
                R.string.required_type_stapes
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        PitchesEditor(pitches)

        BuilderField(
            liveData = model.builder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        ReBuildersEditor(reBuilders)

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            val richEditorState = rememberRichTextState()

            LaunchedEffect(richEditorState) {
                snapshotFlow { richEditorState.toMarkdown() }
                    .collect { model.description.postValue(it) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.form_description),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = showDescription,
                    onCheckedChange = { model.showDescription.value = it },
                    enabled = richEditorState.toMarkdown().isNotEmpty()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentSpanStyle = richEditorState.currentSpanStyle

                EditorToolbarButton(
                    richTextState = richEditorState,
                    currentSpanStyle = currentSpanStyle,
                    icon = Icons.Rounded.FormatBold,
                    tooltip = R.string.rich_editor_bold,
                    check = { it.fontWeight == FontWeight.Bold },
                    span = {
                        SpanStyle(fontWeight = if (it) FontWeight.Normal else FontWeight.Bold)
                    }
                )
                EditorToolbarButton(
                    richTextState = richEditorState,
                    currentSpanStyle = currentSpanStyle,
                    icon = Icons.Rounded.FormatItalic,
                    tooltip = R.string.rich_editor_italic,
                    check = { it.fontStyle == FontStyle.Italic },
                    span = {
                        SpanStyle(fontStyle = if (it) FontStyle.Normal else FontStyle.Italic)
                    }
                )
                EditorToolbarButton(
                    richTextState = richEditorState,
                    currentSpanStyle = currentSpanStyle,
                    icon = Icons.Rounded.FormatUnderlined,
                    tooltip = R.string.rich_editor_underline,
                    check = { it.textDecoration == TextDecoration.Underline },
                    span = {
                        SpanStyle(textDecoration = if (it) TextDecoration.Underline else TextDecoration.None)
                    }
                )
                EditorToolbarButton(
                    richTextState = richEditorState,
                    currentSpanStyle = currentSpanStyle,
                    icon = Icons.Rounded.Title,
                    tooltip = R.string.rich_editor_title,
                    check = { it.fontSize == 28.sp },
                    span = {
                        if (it)
                            SpanStyle()
                        else
                            SpanStyle(fontSize = 28.sp)
                    }
                )
            }

            RichTextEditor(state = richEditorState, modifier = Modifier.fillMaxWidth())
        }
    }

    @Composable
    fun PitchesEditor(pitches: MutableList<PitchInfo>) {
        val localDensity = LocalDensity.current

        var addButtonWidth by remember { mutableStateOf(0.dp) }
        var deleteButtonWidth by remember { mutableStateOf(0.dp) }

        FormListCreator(
            list = pitches,
            reorderableState = { state ->
                rememberReorderableLazyListState(
                    listState = state,
                    onMove = model::movePitch
                )
            },
            title = stringResource(R.string.form_pitches_title),
            inputHeadline = {
                Text(
                    text = stringResource(R.string.form_grade),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_height),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending_info),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending_inclination),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "",
                    modifier = Modifier.width(addButtonWidth)
                )
            },
            inputContent = {
                var pitchGrade by remember { mutableStateOf<GradeValue?>(null) }
                var pitchHeight by remember { mutableStateOf<String?>(null) }
                var pitchEnding by remember { mutableStateOf<Ending?>(null) }
                var pitchEndingInfo by remember { mutableStateOf<EndingInfo?>(null) }
                var pitchEndingInclination by remember { mutableStateOf<EndingInclination?>(null) }

                FormDropdown(
                    selection = pitchGrade,
                    onSelectionChanged = { value ->
                        pitchGrade = value.takeUnless { pitchGrade == it }
                    },
                    options = listOf(
                        *SportsGrade.entries.toTypedArray(),
                        *ArtificialGrade.entries.toTypedArray()
                    ),
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { it.displayName }

                FormField(
                    value = pitchHeight,
                    onValueChange = { pitchHeight = it.takeIf { it.isNotBlank() } },
                    label = null,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    valueAssertion = ValueAssertion.NUMBER
                )

                FormDropdown(
                    selection = pitchEnding,
                    onSelectionChanged = { value ->
                        pitchEnding = value.takeUnless { pitchEnding == it }
                    },
                    options = Ending.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                FormDropdown(
                    selection = pitchEndingInfo,
                    onSelectionChanged = { value ->
                        pitchEndingInfo = value.takeUnless { pitchEndingInfo == it }
                    },
                    options = EndingInfo.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                FormDropdown(
                    selection = pitchEndingInclination,
                    onSelectionChanged = { value ->
                        pitchEndingInclination = value.takeUnless { pitchEndingInclination == it }
                    },
                    options = EndingInclination.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                IconButton(
                    onClick = {
                        val pitchInfo = PitchInfo(
                            0U,
                            pitchGrade,
                            pitchHeight?.toUIntOrNull(),
                            pitchEnding,
                            pitchEndingInfo,
                            pitchEndingInclination
                        )
                        model.pitches.add(pitchInfo)

                        // Clear all fields
                        pitchGrade = null
                        pitchHeight = null
                        pitchEnding = null
                        pitchEndingInfo = null
                        pitchEndingInclination = null
                    },
                    enabled = pitchGrade != null ||
                        (pitchHeight != null && pitchHeight?.toUIntOrNull() != null) ||
                        pitchEnding != null ||
                        pitchEndingInfo != null ||
                        pitchEndingInclination != null,
                    modifier = Modifier.onGloballyPositioned {
                        addButtonWidth = with(localDensity) { it.size.width.toDp() }
                    }
                ) {
                    Icon(Icons.Rounded.Add, stringResource(R.string.action_add))
                }
            },
            rowHeadline = {
                Text(
                    text = stringResource(R.string.form_sketch_id_short),
                    modifier = Modifier.width(32.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_grade),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_height),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending_info),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = stringResource(R.string.form_ending_inclination),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "",
                    modifier = Modifier.width(deleteButtonWidth)
                )
            },
            rowContent = { index, item ->
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )

                FormDropdown(
                    selection = item.gradeValue,
                    onSelectionChanged = { value ->
                        model.modifyPitch(
                            index,
                            value,
                            { it.gradeValue },
                            { i, v -> i.copy(gradeValue = v) })
                    },
                    options = listOf(
                        *SportsGrade.entries.toTypedArray(),
                        *ArtificialGrade.entries.toTypedArray()
                    ),
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { it.displayName }

                FormField(
                    value = item.height?.toString(),
                    onValueChange = { value ->
                        val newValue = value.toUIntOrNull() ?: return@FormField
                        model.modifyPitch(
                            index,
                            newValue,
                            { it.height },
                            { i, v -> i.copy(height = v) })
                    },
                    label = null,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    valueAssertion = ValueAssertion.NUMBER
                )

                FormDropdown(
                    selection = item.ending,
                    onSelectionChanged = { value ->
                        model.modifyPitch(
                            index,
                            value,
                            { it.ending },
                            { i, v -> i.copy(ending = v) })
                    },
                    options = Ending.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                FormDropdown(
                    selection = item.info,
                    onSelectionChanged = { value ->
                        model.modifyPitch(
                            index,
                            value,
                            { it.info },
                            { i, v -> i.copy(info = v) })
                    },
                    options = EndingInfo.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                FormDropdown(
                    selection = item.inclination,
                    onSelectionChanged = { value ->
                        model.modifyPitch(
                            index,
                            value,
                            { it.inclination },
                            { i, v -> i.copy(inclination = v) })
                    },
                    options = EndingInclination.entries,
                    label = null,
                    modifier = Modifier.weight(1f)
                ) { stringResource(it.displayName) }

                IconButton(
                    onClick = {
                        model.pitches.removeAt(index)
                    },
                    modifier = Modifier.onGloballyPositioned {
                        deleteButtonWidth = with(localDensity) { it.size.width.toDp() }
                    }
                ) {
                    Icon(Icons.Outlined.DeleteForever, stringResource(R.string.action_remove))
                }
            }
        )
    }

    @Composable
    fun CountField(
        count: String?,
        onValueChange: (String?) -> Unit,
        @StringRes label: Int
    ) {
        val isKnown = count?.toIntOrNull()?.let { it < SafesCount.MANY_SAFES } ?: true
        FormField(
            value = count.takeIf { isKnown },
            onValueChange = { onValueChange(it.takeIf { it.isNotBlank() }) },
            label = stringResource(label),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            enabled = isKnown,
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER,
            trailingContent = {
                PlainTooltipBox(tooltip = { Text(stringResource(R.string.form_count_unknown)) }) {
                    Checkbox(
                        checked = !isKnown,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onValueChange(Int.MAX_VALUE.toString())
                            } else {
                                onValueChange(null)
                            }
                        },
                        modifier = Modifier.tooltipAnchor()
                    )
                }
            },
            supportingText = stringResource(R.string.form_count_unknown_support)
        )
    }

    @Composable
    fun RequiredField(
        isRequired: Boolean?,
        onValueChange: (Boolean) -> Unit,
        @StringRes label: Int
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isRequired ?: false, onCheckedChange = onValueChange)

            Text(
                text = stringResource(label),
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    fun BuilderField(
        liveData: MutableLiveData<Builder>,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
        ) {
            val builder by liveData.observeAsState()

            FormField(
                value = builder?.name,
                onValueChange = { text ->
                    val value = text.takeIf { it.isNotBlank() }
                    liveData.value = builder?.copy(name = value) ?: Builder(value)
                },
                label = stringResource(R.string.form_builder_name),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FormField(
                value = builder?.date,
                onValueChange = { text ->
                    val value = text.takeIf { it.isNotBlank() }
                    liveData.value = builder?.copy(date = value) ?: Builder(null, value)
                },
                label = stringResource(R.string.form_builder_date),
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    fun ReBuildersEditor(reBuilders: List<Builder>) {
        FormListCreator(
            list = reBuilders,
            title = stringResource(R.string.form_re_buildings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            inputContent = {
                val reBuilder = MutableLiveData<Builder>()

                BuilderField(liveData = reBuilder, modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        model.reBuilders.value = reBuilders.toMutableList().apply {
                            reBuilder.value
                        }
                    },
                    enabled = reBuilder.value?.let { it.name != null || it.date != null } ?: false
                ) {
                    Icon(Icons.Rounded.Add, stringResource(R.string.action_add))
                }
            },
            rowContent = { index, item ->
                Text(
                    text = item.name ?: stringResource(R.string.none),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = item.date ?: stringResource(R.string.none),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge
                )

                IconButton(
                    onClick = {
                        model.reBuilders.value = reBuilders.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                ) {
                    Icon(Icons.Outlined.DeleteForever, stringResource(R.string.action_remove))
                }
            }
        )
    }

    @Composable
    fun EditorToolbarButton(
        richTextState: RichTextState,
        currentSpanStyle: SpanStyle,
        icon: ImageVector,
        @StringRes tooltip: Int,
        check: (SpanStyle) -> Boolean,
        span: (Boolean) -> SpanStyle
    ) {
        PlainTooltipBox(tooltip = { Text(stringResource(tooltip)) }) {
            IconButton(
                onClick = {
                    richTextState.toggleSpanStyle(
                        span(check(currentSpanStyle))
                    )
                },
                modifier = Modifier.tooltipAnchor(),
                enabled = richTextState.selection.length > 0
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(tooltip),
                    tint = if (check(currentSpanStyle))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    class Model(
        application: Application,
        sectorId: Long,
        pathId: Long?,
        override val whenNotFound: suspend () -> Unit
    ) : EditorModel<Sector, Path>(application, sectorId, pathId) {
        companion object {
            fun Factory(
                sectorId: Long,
                pathId: Long?,
                whenSectorNotFound: () -> Unit
            ): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application =
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                    Model(application, sectorId, pathId, whenSectorNotFound)
                }
            }
        }

        override val elementSerializer: JsonSerializer<Path> = Path.Companion

        val sectorImage = MutableLiveData<LocalFile>()
        val sectorImageProgress = MutableLiveData<Float>()

        override val creatorEndpoint: String = "path"

        val displayName = MutableLiveData<String>()
        val sketchId = MutableLiveData<String>()

        val height = MutableLiveData<String>()
        val grade = MutableLiveData<GradeValue>()
        val ending = MutableLiveData<Ending>()

        val pitches = mutableStateListOf<PitchInfo>()

        val stringCount = MutableLiveData<String>()

        val paraboltCount = MutableLiveData<String>()
        val burilCount = MutableLiveData<String>()
        val pitonCount = MutableLiveData<String>()
        val spitCount = MutableLiveData<String>()
        val tensorCount = MutableLiveData<String>()

        val crackerRequired = MutableLiveData(false)
        val friendRequired = MutableLiveData(false)
        val lanyardRequired = MutableLiveData(false)
        val nailRequired = MutableLiveData(false)
        val pitonRequired = MutableLiveData(false)
        val stapesRequired = MutableLiveData(false)

        val showDescription = MutableLiveData(false)
        val description = MutableLiveData<String>()

        val builder = MutableLiveData<Builder>()
        val reBuilders = MutableLiveData<List<Builder>>()

        override val hasParent: Boolean = true

        override suspend fun init(parent: Sector) {
            // initialize sketchId as the last one of the sector's paths
            val paths = dao.getPathsFromSector(parent.id)
            if (paths != null) {
                this@Model.sketchId.postValue(
                    try {
                        (paths.paths.maxOf { it.sketchId } + 1).toString()
                    } catch (_: NoSuchElementException) {
                        "1"
                    }
                )
            }

            // Load sector image
            parent.fetchImage(getApplication(), null) { c, m ->
                sectorImageProgress.postValue(m.toFloat() / c)
            }.collect {
                sectorImage.postValue(it)
            }
        }

        override suspend fun fill(child: Path) {
            displayName.postValue(child.displayName)
            sketchId.postValue(child.sketchId.toString())

            height.postValue(child.height?.toString())
            grade.postValue(child.grade)
            ending.postValue(child.ending)

            child.pitches?.let(pitches::addAll)

            stringCount.postValue(child.stringCount?.toString())

            paraboltCount.postValue(child.paraboltCount?.toString())
            burilCount.postValue(child.burilCount?.toString())
            pitonCount.postValue(child.pitonCount?.toString())
            spitCount.postValue(child.spitCount?.toString())
            tensorCount.postValue(child.tensorCount?.toString())

            crackerRequired.postValue(child.crackerRequired)
            friendRequired.postValue(child.friendRequired)
            lanyardRequired.postValue(child.lanyardRequired)
            nailRequired.postValue(child.nailRequired)
            pitonRequired.postValue(child.pitonRequired)
            stapesRequired.postValue(child.stapesRequired)

            showDescription.postValue(child.showDescription)
            description.postValue(child.description)

            builder.postValue(child.builder)
            reBuilders.postValue(child.reBuilder)
        }

        private fun checkRequirements(): Boolean {
            return displayName.value != null && sketchId.value != null
        }

        override val isFilled: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(sketchId) { value = checkRequirements() }
        }

        override suspend fun fetchParent(parentId: Long): Sector? = dao.getSector(parentId)

        override suspend fun fetchChild(childId: Long): Path? = dao.getPath(childId)

        override fun FormBuilder.getFormData() {
            Timber.i("Creating a new path for sector #$parentId")

            appendDifference("displayName", displayName.value, element.value?.displayName)
            appendDifference("sketchId", sketchId.value, element.value?.sketchId)

            appendDifference("height", height.value?.toULongOrNull(), element.value?.height)
            appendDifference("grade", grade.value, element.value?.grade)
            appendDifference("ending", ending.value, element.value?.ending)

            appendDifference("pitches", pitches, element.value?.pitches)

            appendDifference("stringCount", stringCount.value, element.value?.stringCount)

            appendDifference("paraboltCount", paraboltCount.value, element.value?.paraboltCount)
            appendDifference("burilCount", burilCount.value, element.value?.burilCount)
            appendDifference("pitonCount", pitonCount.value, element.value?.pitonCount)
            appendDifference("spitCount", spitCount.value, element.value?.spitCount)
            appendDifference("tensorCount", tensorCount.value, element.value?.tensorCount)

            appendDifference("crackerRequired", crackerRequired.value, element.value?.crackerRequired)
            appendDifference("friendRequired", friendRequired.value, element.value?.friendRequired)
            appendDifference("lanyardRequired", lanyardRequired.value, element.value?.lanyardRequired)
            appendDifference("nailRequired", nailRequired.value, element.value?.nailRequired)
            appendDifference("pitonRequired", pitonRequired.value, element.value?.pitonRequired)
            appendDifference("stapesRequired", stapesRequired.value, element.value?.stapesRequired)

            appendDifference("showDescription", showDescription.value, element.value?.showDescription)
            appendDifference("description", description.value, element.value?.description)

            appendDifference("builder", builder.value, element.value?.builder)
            appendDifference("reBuilders", reBuilders.value, element.value?.reBuilder)

            append("sector", parentId!!)
        }

        @UiThread
        fun <T> modifyPitch(
            index: Int,
            value: T,
            property: (PitchInfo) -> T,
            copy: (item: PitchInfo, value: T?) -> PitchInfo
        ) {
            val item = pitches.removeAt(index)
            pitches.add(
                index,
                if (property(item) == value)
                    copy(item, null)
                else
                    copy(item, value)
            )
        }

        fun movePitch(from: ItemPosition, to: ItemPosition) {
            try {
                Timber.d("Moving pitch from ${from.index} to ${to.index}")
                pitches.add(to.index, pitches.removeAt(from.index))
            } catch (_: IndexOutOfBoundsException) {
                Timber.w("Could not move pitch from ${from.index} to ${to.index}: Out of bounds")
            }
        }

        override suspend fun insertDao(element: Path) = dao.insert(element)

        override suspend fun updateDao(element: Path) = dao.update(element)
    }
}