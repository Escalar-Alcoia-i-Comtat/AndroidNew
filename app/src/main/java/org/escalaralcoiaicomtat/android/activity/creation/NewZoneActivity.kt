package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormIconDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.ui.form.FormKMZPicker
import org.escalaralcoiaicomtat.android.ui.form.FormListCreator
import org.escalaralcoiaicomtat.android.ui.form.PointOption
import org.escalaralcoiaicomtat.android.utils.appendDifference

@OptIn(ExperimentalFoundationApi::class)
class NewZoneActivity : EditorActivity<Area, Zone, NewZoneActivity.Model>(R.string.new_zone_title) {

    object Contract : ResultContract<NewZoneActivity>(NewZoneActivity::class)

    override val model: Model by viewModels { Model.Factory(parentId!!, elementId, ::onBack) }

    @Composable
    override fun ColumnScope.Content() {
        val displayName by model.displayName.observeAsState(initial = "")
        val webUrl by model.webUrl.observeAsState(initial = "")
        val image by model.image.observeAsState()
        val kmzFile by model.kmzName.observeAsState()
        val latitude by model.latitude.observeAsState(initial = "")
        val longitude by model.longitude.observeAsState(initial = "")
        val points by model.points.observeAsState()

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
        FormField(
            value = webUrl,
            onValueChange = { model.webUrl.value = it },
            label = stringResource(R.string.form_web_url),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Uri,
            keyboardCapitalization = KeyboardCapitalization.None,
            thisFocusRequester = webUrlFocusRequester
        )

        FormImagePicker(image, contentDescription = displayName, model.isLoadingImage) {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        FormKMZPicker(
            fileName = kmzFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            kmzPicker.launch(arrayOf("application/vnd.google-earth.kmz"))
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

        FormListCreator(
            list = points ?: emptyList(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            inputContent = {
                var selection: PointOption by remember { mutableStateOf(PointOptions.Default) }
                var label: String by remember { mutableStateOf("") }
                var rowLatitude: String by remember { mutableStateOf("") }
                var rowLongitude: String by remember { mutableStateOf("") }

                val rowLatitudeFocusRequester = remember { FocusRequester() }
                val rowLongitudeFocusRequester = remember { FocusRequester() }

                FormIconDropdown(
                    selection = selection,
                    onSelectionChanged = { selection = it },
                    options = PointOptions.All.toList()
                )
                FormField(
                    value = label,
                    onValueChange = { label = it },
                    label = stringResource(R.string.form_point_label),
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 8.dp, end = 4.dp),
                    nextFocusRequester = rowLatitudeFocusRequester
                )
                FormField(
                    value = rowLatitude,
                    onValueChange = { rowLatitude = it },
                    label = stringResource(R.string.form_latitude),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    thisFocusRequester = rowLatitudeFocusRequester,
                    nextFocusRequester = rowLongitudeFocusRequester,
                    keyboardType = KeyboardType.Number
                )
                FormField(
                    value = rowLongitude,
                    onValueChange = { rowLongitude = it },
                    label = stringResource(R.string.form_longitude),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    thisFocusRequester = rowLongitudeFocusRequester,
                    keyboardType = KeyboardType.Number
                )

                IconButton(
                    enabled = label.isNotBlank() &&
                        rowLatitude.toDoubleOrNull() != null &&
                        rowLongitude.toDoubleOrNull() != null,
                    onClick = {
                        model.points.postValue(
                            (points ?: emptyList()).toMutableList().apply {
                                add(
                                    DataPoint(
                                        LatLng(rowLatitude.toDouble(), rowLongitude.toDouble()),
                                        label,
                                        selection.key
                                    )
                                )
                            }
                        )
                        rowLatitude = ""
                        rowLongitude = ""
                        label = ""
                        selection = PointOptions.Default
                    }
                ) {
                    Icon(Icons.Rounded.Add, stringResource(R.string.action_add))
                }
            },
            rowContent = { _, point ->
                ListItem(
                    leadingContent = {
                        PointOptions.valueOf(point.icon)?.let { pointData ->
                            Icon(pointData.icon, point.label)
                        }
                    },
                    headlineContent = { Text(point.label) },
                    supportingContent = {
                        Text(
                            text = point.location.let { "${it.latitude}, ${it.longitude}" }
                        )
                    },
                    trailingContent = {
                        IconButton(
                            onClick = {
                                model.points.postValue(
                                    (points ?: emptyList()).toMutableList().apply {
                                        remove(point)
                                    }.takeIf { it.isNotEmpty() }
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.Close, stringResource(R.string.action_remove))
                        }
                    }
                )
            }
        )
    }

    object PointOptions {
        val Default = PointOption("default", Icons.Outlined.LocationOn, R.string.icon_label_default)
        val Parking = PointOption("parking", Icons.Outlined.LocalParking, R.string.icon_label_parking)
        val Park = PointOption("park", Icons.Outlined.Park, R.string.icon_label_park)
        val Water = PointOption("water", Icons.Outlined.WaterDrop, R.string.icon_label_water)
        val Pool = PointOption("pool", Icons.Outlined.Pool, R.string.icon_label_pool)
        val Restaurant = PointOption("restaurant", Icons.Outlined.Restaurant, R.string.icon_label_restaurant)
        val Hotel = PointOption("hotel", Icons.Outlined.Hotel, R.string.icon_label_hotel)

        val All = arrayOf(
            Default, Parking, Park, Water, Pool, Restaurant, Hotel
        )

        fun valueOf(key: String) = All.find { it.key == key }
    }

    class Model(
        application: Application,
        areaId: Long,
        zoneId: Long?,
        override val whenNotFound: suspend () -> Unit
    ) : EditorModel<Area, Zone>(application, areaId, zoneId) {
        companion object {
            fun Factory(
                areaId: Long,
                zoneId: Long?,
                whenNotFound: () -> Unit
            ): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as Application
                    Model(application, areaId, zoneId, whenNotFound)
                }
            }
        }

        override val creatorEndpoint: String = "zone"

        override val hasParent: Boolean = true

        val displayName = MutableLiveData("")
        val webUrl = MutableLiveData("")
        val latitude = MutableLiveData("")
        val longitude = MutableLiveData("")
        val points = MutableLiveData<List<DataPoint>>()

        private fun checkRequirements(): Boolean {
            return displayName.value?.isNotBlank() == true &&
                webUrl.value?.isNotBlank() == true &&
                image.value != null &&
                kmzName.value != null &&
                latitude.value?.toDoubleOrNull() != null &&
                longitude.value?.toDoubleOrNull() != null
        }

        override val isFilled: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(webUrl) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
            addSource(kmzName) { value = checkRequirements() }
            addSource(latitude) { value = checkRequirements() }
            addSource(longitude) { value = checkRequirements() }
        }

        override suspend fun fill(child: Zone) {
            displayName.postValue(child.displayName)
            webUrl.postValue(child.webUrl.toString())
            latitude.postValue(child.point?.latitude?.toString())
            longitude.postValue(child.point?.longitude?.toString())
            points.postValue(child.points)
        }

        override suspend fun fetchParent(parentId: Long): Area? = dao.getArea(parentId)

        override suspend fun fetchChild(childId: Long): Zone? = dao.getZone(childId)

        override fun FormBuilder.getFormData() {
            appendDifference("displayName", displayName.value, element.value?.displayName)
            appendDifference("webUrl", webUrl.value, element.value?.webUrl)
            appendDifference(
                "point",
                LatLng(latitude.value!!.toDouble(), longitude.value!!.toDouble()),
                element.value?.point
            )
            appendDifference("points", points, element.value?.points)
            append("area", parentId!!)
        }
    }
}
