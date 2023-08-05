package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.ui.form.FormCheckbox
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.ui.form.FormSegmentedButton
import org.escalaralcoiaicomtat.android.ui.form.SizeMode
import org.escalaralcoiaicomtat.android.utils.appendSerializable
import timber.log.Timber

class NewSectorActivity : CreatorActivity<NewSectorActivity.Model>(R.string.new_sector_title) {

    object Contract : ResultContract<NewSectorActivity>(NewSectorActivity::class)

    private val parentName: String? by extras()
    private val parentId: Long? by extras()

    override val model: Model by viewModels { Model.Factory(parentId!!) }

    @Composable
    override fun ColumnScope.Content() {
        val displayName by model.displayName.observeAsState(initial = "")
        val image by model.image.observeAsState()
        val latitude by model.latitude.observeAsState(initial = "")
        val longitude by model.longitude.observeAsState(initial = "")
        val kidsApt by model.kidsApt.observeAsState(initial = false)
        val walkingTime by model.walkingTime.observeAsState(initial = "")

        val webUrlFocusRequester = remember { FocusRequester() }
        val latitudeFocusRequester = remember { FocusRequester() }
        val longitudeFocusRequester = remember { FocusRequester() }

        FormField(
            value = parentName ?: "",
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
            nextFocusRequester = webUrlFocusRequester
        )

        FormImagePicker(image, contentDescription = displayName, model.isLoadingImage) {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            FormField(
                value = latitude,
                onValueChange = { model.latitude.value = it },
                label = stringResource(R.string.form_latitude),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                nextFocusRequester = longitudeFocusRequester,
                thisFocusRequester = latitudeFocusRequester,
                keyboardType = KeyboardType.Number
            )
            FormField(
                value = longitude,
                onValueChange = { model.longitude.value = it },
                label = stringResource(R.string.form_longitude),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                thisFocusRequester = longitudeFocusRequester,
                keyboardType = KeyboardType.Number
            )
        }

        FormSegmentedButton(
            items = SunTime.entries.map { stringResource(it.label) },
            label = stringResource(R.string.form_sun_time),
            modifier = Modifier.padding(top = 8.dp),
            mode = SizeMode.FILL_MAX_WIDTH,
            onItemSelection = { model.sunTime.postValue(SunTime.entries[it]) }
        )

        FormCheckbox(
            checked = kidsApt,
            onCheckedChange = { model.kidsApt.postValue(it) },
            label = stringResource(R.string.form_kids_apt)
        )

        FormField(
            value = walkingTime,
            onValueChange = { model.walkingTime.value = it },
            label = stringResource(R.string.form_walking_time),
            modifier = Modifier.fillMaxWidth()
        )
    }

    class Model(
        application: Application,
        private val zoneId: Long
    ) : CreatorModel(application) {
        companion object {
            fun Factory(zoneId: Long): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                    Model(application, zoneId)
                }
            }
        }

        override val creatorEndpoint: String = "sector"

        val displayName = MutableLiveData("")
        val kidsApt = MutableLiveData(false)
        val sunTime = MutableLiveData(SunTime.None)
        val walkingTime = MutableLiveData("")
        val latitude = MutableLiveData("")
        val longitude = MutableLiveData("")
        val weight = MutableLiveData("")

        private fun checkRequirements(): Boolean {
            return displayName.value?.isNotBlank() == true &&
                image.value != null &&
                latitude.value?.toDoubleOrNull() != null &&
                longitude.value?.toDoubleOrNull() != null
        }

        override val isFilled: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
            addSource(latitude) { value = checkRequirements() }
            addSource(longitude) { value = checkRequirements() }
        }

        override fun FormBuilder.getFormData() {
            Timber.i("Creating a new sector for zone #${zoneId.toInt()}")

            append("displayName", displayName.value!!)
            appendSerializable("point", LatLng(latitude.value!!, longitude.value!!))
            append("kidsApt", kidsApt.value ?: false)
            append("sunTime", sunTime.value!!.name)
            walkingTime.value
                ?.takeIf { it.isNotBlank() }
                ?.toLongOrNull()
                ?.let { append("walkingTime", it) }
            append("zone", zoneId)
        }
    }
}
