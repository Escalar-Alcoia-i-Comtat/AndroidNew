package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.burnoutcrew.reorderable.ItemPosition
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.escalaralcoiaicomtat.android.R
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
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class NewPathActivity : CreatorActivity<NewPathActivity.Model>(R.string.new_path_title) {
    object Contract : ResultContract<NewPathActivity>(NewPathActivity::class)

    private val parentId: Long? by extras()

    override val model: Model by viewModels {
        Model.Factory(parentId!!, ::onBack)
    }

    override val isScrollable: Boolean = false

    override val maxWidth: Int = 1200

    @Composable
    override fun ColumnScope.Content() {
        val sector by model.sector.observeAsState()

        sector?.let { SplitView(it) } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }

    @Composable
    fun SplitView(sector: Sector) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
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

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
                    .verticalScroll(rememberScrollState())
            ) {
                Editor(sector)
            }
        }
    }

    @Composable
    fun Editor(sector: Sector) {
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

        val sketchIdFocusRequester = remember { FocusRequester() }
        val heightFocusRequester = remember { FocusRequester() }

        FormField(
            value = sector.displayName,
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

        PitchesEditor(pitches)

        // TODO: builder
        // TODO: re-builders
        // TODO: description & showDescription
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

    class Model(
        application: Application,
        private val sectorId: Long,
        whenSectorNotFound: () -> Unit
    ) : CreatorModel(application) {
        companion object {
            fun Factory(
                sectorId: Long,
                whenSectorNotFound: () -> Unit
            ): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application =
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                    Model(application, sectorId, whenSectorNotFound)
                }
            }
        }

        init {
            viewModelScope.launch(Dispatchers.IO) {
                val sector = dao.getSector(sectorId)
                if (sector == null) {
                    Timber.e("Could not get a valid sector with id $sectorId")
                    whenSectorNotFound()
                    return@launch
                }
                this@Model.sector.postValue(sector)

                // initialize sketchId as the last one of the sector's paths
                val paths = dao.getPathsFromSector(sectorId)
                if (paths != null) {
                    this@Model.sketchId.postValue(
                        (paths.paths.maxOf { it.sketchId } + 1).toString()
                    )
                }

                // Load sector image
                sector.fetchImage(application, null) { c, m ->
                    sectorImageProgress.postValue(m.toFloat() / c)
                }.collect {
                    sectorImage.postValue(it)
                }
            }
        }

        val sector = MutableLiveData<Sector>()

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
        val reBuilder = MutableLiveData<List<Builder>>()

        private fun checkRequirements(): Boolean {
            return displayName.value != null && sketchId.value != null
        }

        override val isFilled: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = checkRequirements() }
        }

        override fun FormBuilder.getFormData() {
            Timber.i("Creating a new path for sector #${sectorId}")

            append("displayName", displayName.value!!)
            append("sketchId", sketchId.value!!)

            // todo - missing fields
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
    }
}