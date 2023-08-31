package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.SafesCount
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.ui.form.FormDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.ValueAssertion
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class NewPathActivity : CreatorActivity<NewPathActivity.Model>(R.string.new_path_title) {
    object Contract : ResultContract<NewPathActivity>(NewPathActivity::class)

    private val parentId: Long? by extras()

    override val model: Model by viewModels {
        Model.Factory(parentId!!, ::onBack)
    }

    @Composable
    override fun ColumnScope.Content() {
        val sector by model.sector.observeAsState()

        sector?.let { Editor(it) } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    }

    @Composable
    fun Editor(sector: Sector) {
        val displayName by model.displayName.observeAsState()
        val sketchId by model.sketchId.observeAsState()

        val height by model.height.observeAsState()
        val grade by model.grade.observeAsState()
        val ending by model.ending.observeAsState()

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

        CountField(paraboltCount, model.paraboltCount::setValue, R.string.safe_type_parabolt)
        CountField(burilCount, model.burilCount::setValue, R.string.safe_type_buril)
        CountField(pitonCount, model.pitonCount::setValue, R.string.safe_type_piton)
        CountField(spitCount, model.spitCount::setValue, R.string.safe_type_spit)
        CountField(tensorCount, model.tensorCount::setValue, R.string.safe_type_tensor)
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
            modifier = Modifier.fillMaxWidth(),
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
            }
        }

        val sector = MutableLiveData<Sector>()

        override val creatorEndpoint: String = "path"

        val displayName = MutableLiveData<String>()
        val sketchId = MutableLiveData<String>()

        val height = MutableLiveData<String>()
        val grade = MutableLiveData<GradeValue>()
        val ending = MutableLiveData<Ending>()

        // todo val pitches = MutableLiveData<Void>()

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
        }
    }
}