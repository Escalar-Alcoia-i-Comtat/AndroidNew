package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.ui.form.FormCheckbox
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.ui.form.FormSegmentedButton
import org.escalaralcoiaicomtat.android.ui.form.SizeMode
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import timber.log.Timber

class NewSectorActivity : EditorActivity<Zone, Sector, Path, NewSectorActivity.Model>(
    createTitleRes = R.string.new_sector_title,
    editTitleRes = R.string.edit_sector_title
) {

    object Contract : ResultContract<NewSectorActivity>(NewSectorActivity::class)

    override val model: Model by viewModels { Model.Factory(parentId!!, elementId, ::onBack) }

    @Composable
    override fun ColumnScope.Editor(parent: Zone?) {
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
            value = parent?.displayName ?: "",
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
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number
        )
    }

    class Model(
        application: Application,
        zoneId: Long,
        sectorId: Long?,
        override val whenNotFound: suspend () -> Unit
    ) : EditorModel<Zone, Sector, Path>(application, zoneId, sectorId) {
        companion object {
            fun Factory(
                zoneId: Long,
                sectorId: Long?,
                whenNotFound: () -> Unit
            ): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                    Model(application, zoneId, sectorId, whenNotFound)
                }
            }
        }

        override val elementSerializer: JsonSerializer<Sector> = Sector.Companion

        override val creatorEndpoint: String = "sector"

        override val hasParent: Boolean = true

        val displayName = MutableLiveData("")
        val kidsApt = MutableLiveData(false)
        val sunTime = MutableLiveData(SunTime.None)
        val walkingTime = MutableLiveData("")
        val latitude = MutableLiveData("")
        val longitude = MutableLiveData("")
        val weight = MutableLiveData("")

        init {
            onInit()
        }

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

        override suspend fun fill(child: Sector) {
            displayName.postValue(child.displayName)
            kidsApt.postValue(child.kidsApt)
            sunTime.postValue(child.sunTime)
            walkingTime.postValue(child.walkingTime?.toString())
            latitude.postValue(child.point?.latitude?.toString())
            longitude.postValue(child.point?.longitude?.toString())
            weight.postValue(child.weight)

            child.readImageFile(getApplication(), lifecycle).collect {
                val bitmap: Bitmap? = it?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                image.postValue(bitmap)
            }
        }

        override suspend fun fetchParent(parentId: Long): Zone? = dao.getZone(parentId)

        override suspend fun fetchChild(childId: Long): Sector? = dao.getSector(childId)

        override fun FormBuilder.getFormData() {
            Timber.i("Creating a new sector for zone #$parentId")

            appendDifference("displayName", displayName.value, element.value?.displayName)
            appendDifference("point", LatLng(latitude.value!!, longitude.value!!), element.value?.point)
            appendDifference("kidsApt", kidsApt.value, element.value?.kidsApt)
            appendDifference("sunTime", sunTime.value, element.value?.sunTime)
            appendDifference("walkingTime", walkingTime.value?.takeIf { it.isNotBlank() }?.toLongOrNull(), element.value?.walkingTime)
            append("zone", parentId!!)
        }

        override suspend fun insert(element: Sector) { dao.insert(element) }

        override suspend fun update(element: Sector) = dao.update(element)
    }
}
