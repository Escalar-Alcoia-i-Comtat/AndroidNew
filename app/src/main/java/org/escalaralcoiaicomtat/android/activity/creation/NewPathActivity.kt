package org.escalaralcoiaicomtat.android.activity.creation

import androidx.activity.viewModels
import androidx.annotation.StringRes
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.type.ArtificialGrade
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.storage.type.SafesCount
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.ui.form.FormDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormListCreator
import org.escalaralcoiaicomtat.android.ui.form.ValueAssertion
import org.escalaralcoiaicomtat.android.viewmodel.editor.PathModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class NewPathActivity : EditorActivity<Sector, Path, BaseEntity, PathModel>(
    createTitleRes = R.string.new_path_title,
    editTitleRes = R.string.edit_path_title
) {
    object Contract : ResultContract<NewPathActivity>(NewPathActivity::class)

    override val model: PathModel by viewModels {
        PathModel.Factory(parentId!!, elementId, ::onBack)
    }

    override val maxWidth: Int = 1200

    @Composable
    override fun RowScope.SidePanel(parent: Sector?) {
        val sector = parent!!

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
                        .data(it)
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
                CircularProgressIndicator()
            }
        }
    }

    @Composable
    override fun ColumnScope.Editor(parent: Sector?) {
        val uiState by model.uiState.collectAsState()
        val displayName = uiState.displayName
        val sketchId = uiState.sketchId

        val height = uiState.height
        val grade = uiState.grade
        val ending = uiState.ending

        val pitches = uiState.pitches

        val stringCount = uiState.stringCount
        val paraboltCount = uiState.paraboltCount
        val burilCount = uiState.burilCount
        val pitonCount = uiState.pitonCount
        val spitCount = uiState.spitCount
        val tensorCount = uiState.tensorCount

        val crackerRequired = uiState.crackerRequired
        val friendRequired = uiState.friendRequired
        val lanyardRequired = uiState.lanyardRequired
        val nailRequired = uiState.nailRequired
        val pitonRequired = uiState.pitonRequired
        val stapesRequired = uiState.stapesRequired

        val reBuilders = uiState.reBuilders

        val showDescription = uiState.showDescription

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
            onValueChange = model::setDisplayName,
            label = stringResource(R.string.form_display_name),
            modifier = Modifier.fillMaxWidth(),
            nextFocusRequester = sketchIdFocusRequester
        )

        FormField(
            value = sketchId,
            onValueChange = model::setSketchId,
            label = stringResource(R.string.form_sketch_id),
            modifier = Modifier.fillMaxWidth(),
            thisFocusRequester = sketchIdFocusRequester,
            nextFocusRequester = heightFocusRequester,
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER
        )

        FormField(
            value = height,
            onValueChange = model::setHeight,
            label = stringResource(R.string.form_height),
            modifier = Modifier.fillMaxWidth(),
            thisFocusRequester = heightFocusRequester,
            keyboardType = KeyboardType.Number,
            valueAssertion = ValueAssertion.NUMBER
        )
        FormDropdown(
            selection = grade,
            onSelectionChanged = model::toggleGrade,
            options = SportsGrade.entries + ArtificialGrade.entries,
            label = stringResource(R.string.form_grade),
            modifier = Modifier.fillMaxWidth()
        ) { it.displayName }
        FormDropdown(
            selection = ending,
            onSelectionChanged = model::toggleEnding,
            options = Ending.entries,
            label = stringResource(R.string.form_ending),
            modifier = Modifier.fillMaxWidth()
        ) { stringResource(it.displayName) }

        FormField(
            value = stringCount,
            onValueChange = model::setStringCount,
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

            CountField(paraboltCount, model::setParaboltCount, R.string.safe_type_parabolt)
            CountField(burilCount, model::setBurilCount, R.string.safe_type_buril)
            CountField(pitonCount, model::setPitonCount, R.string.safe_type_piton)
            CountField(spitCount, model::setSpitCount, R.string.safe_type_spit)
            CountField(tensorCount, model::setTensorCount, R.string.safe_type_tensor)

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
                model::setCrackerRequired,
                R.string.required_type_cracker
            )
            RequiredField(
                friendRequired,
                model::setFriendRequired,
                R.string.required_type_friend
            )
            RequiredField(
                lanyardRequired,
                model::setLanyardRequired,
                R.string.required_type_lanyard
            )
            RequiredField(
                nailRequired,
                model::setNailRequired,
                R.string.required_type_nail
            )
            RequiredField(
                pitonRequired,
                model::setPitonRequired,
                R.string.required_type_piton
            )
            RequiredField(
                stapesRequired,
                model::setStapesRequired,
                R.string.required_type_stapes
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        PitchesEditor(pitches ?: emptyList())

        BuilderField(
            builder = uiState.builder,
            onValueChange = model::setBuilder,
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
                snapshotFlow { richEditorState.annotatedString }
                    .collect { model.setDescription(richEditorState.toMarkdown()) }
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
                    onCheckedChange = model::setShowDescription,
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
    fun PitchesEditor(pitches: List<PitchInfo>) {
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
            dialog = { indexInfo ->
                val index = indexInfo?.first
                val info = indexInfo?.second
                var pitchGrade by remember { mutableStateOf(info?.gradeValue) }
                var pitchHeight by remember { mutableStateOf(info?.height?.toString()) }
                var pitchEnding by remember { mutableStateOf(info?.ending) }

                FormDropdown(
                    selection = pitchGrade,
                    onSelectionChanged = { value ->
                        pitchGrade = value.takeUnless { pitchGrade == it }
                    },
                    options = SportsGrade.entries + ArtificialGrade.entries,
                    label = stringResource(R.string.form_grade),
                    toString = { it.displayName }
                )

                FormField(
                    value = pitchHeight,
                    onValueChange = { pitchHeight = it.takeIf { it.isNotBlank() } },
                    label = stringResource(R.string.form_height),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Number,
                    valueAssertion = ValueAssertion.NUMBER
                )

                FormDropdown(
                    selection = pitchEnding,
                    onSelectionChanged = { value ->
                        pitchEnding = value.takeUnless { pitchEnding == it }
                    },
                    options = Ending.entries,
                    label = stringResource(R.string.form_ending),
                    toString = { stringResource(it.displayName) }
                )

                OutlinedButton(
                    onClick = {
                        val pitchInfo = PitchInfo(
                            info?.pitch ?: 0U,
                            pitchGrade,
                            pitchHeight?.toUIntOrNull(),
                            pitchEnding
                        )
                        val list = (model.uiState.value.pitches ?: emptyList()).toMutableList()
                        if (index != null) {
                            list[index] = pitchInfo
                        } else {
                            list.add(pitchInfo)
                        }
                        model.setPitches(list)

                        // Clear all fields
                        pitchGrade = null
                        pitchHeight = null
                        pitchEnding = null

                        dismiss()
                    },
                    enabled = pitchGrade != null ||
                        (pitchHeight != null && pitchHeight?.toUIntOrNull() != null) ||
                        pitchEnding != null,
                    modifier = Modifier.onGloballyPositioned {
                        addButtonWidth = with(localDensity) { it.size.width.toDp() }
                    }
                ) {
                    Text(
                        text = stringResource(
                            if (info != null) R.string.action_update else R.string.action_add
                        )
                    )
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

                Text(
                    text = item.gradeValue?.displayName ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = item.height?.toString() ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = item.ending?.displayName?.let { stringResource(it) } ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        val list = (model.uiState.value.pitches ?: emptyList()).toMutableList()
                        list.removeAt(index)
                        model.setPitches(list)
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
                TooltipBox(
                    state = rememberTooltipState(),
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.form_count_unknown))
                        }
                    }
                ) {
                    Checkbox(
                        checked = !isKnown,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onValueChange(Int.MAX_VALUE.toString())
                            } else {
                                onValueChange(null)
                            }
                        }
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
        builder: Builder?,
        onValueChange: (Builder) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
        ) {
            FormField(
                value = builder?.name,
                onValueChange = { text ->
                    val value = text.takeIf { it.isNotBlank() }
                    onValueChange(builder?.copy(name = value) ?: Builder(value))
                },
                label = stringResource(R.string.form_builder_name),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            FormField(
                value = builder?.date,
                onValueChange = { text ->
                    val value = text.takeIf { it.isNotBlank() }
                    onValueChange(builder?.copy(date = value) ?: Builder(null, value))
                },
                label = stringResource(R.string.form_builder_date),
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    fun ReBuildersEditor(reBuilders: List<Builder>?) {
        FormListCreator(
            list = reBuilders ?: emptyList(),
            title = stringResource(R.string.form_re_buildings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            dialog = {
                var reBuilder by remember { mutableStateOf<Builder?>(null) }

                BuilderField(
                    builder = reBuilder,
                    onValueChange = { reBuilder = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        val list = (reBuilders ?: emptyList()).toMutableList().apply {
                            add(reBuilder ?: return@OutlinedButton)
                        }
                        model.setReBuilders(list)
                        dismiss()
                    },
                    enabled = reBuilder?.let { it.name != null || it.date != null } ?: false
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
                        val list = (reBuilders ?: emptyList()).toMutableList().apply {
                            removeAt(index)
                        }
                        model.setReBuilders(list)
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
        TooltipBox(
            state = rememberTooltipState(),
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(stringResource(tooltip))
                }
            }
        ) {
            IconButton(
                onClick = {
                    richTextState.toggleSpanStyle(
                        span(check(currentSpanStyle))
                    )
                },
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
}