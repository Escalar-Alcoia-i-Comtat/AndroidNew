package org.escalaralcoiaicomtat.android.activity.creation

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
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.PointOptions
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormIconDropdown
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.ui.form.FormKMZPicker
import org.escalaralcoiaicomtat.android.ui.form.FormListCreator
import org.escalaralcoiaicomtat.android.ui.form.PointOption
import org.escalaralcoiaicomtat.android.viewmodel.editor.ZoneModel

@OptIn(ExperimentalFoundationApi::class)
class NewZoneActivity : EditorActivity<Area, Zone, Sector, ZoneModel>(
    createTitleRes = R.string.new_zone_title,
    editTitleRes = R.string.edit_zone_title
) {

    object Contract : ResultContract<NewZoneActivity>(NewZoneActivity::class)

    override val model: ZoneModel by viewModels { ZoneModel.Factory(parentId!!, elementId, ::onBack) }

    @Composable
    override fun ColumnScope.Editor(parent: Area?) {
        val uiState by model.uiState.collectAsState()

        val displayName = uiState.displayName
        val webUrl = uiState.webUrl
        val image = uiState.image
        val kmzFile = uiState.kmzName
        val latitude = uiState.latitude
        val longitude = uiState.longitude
        val points = uiState.points

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
            onValueChange = model::setDisplayName,
            label = stringResource(R.string.form_display_name),
            modifier = Modifier.fillMaxWidth(),
            nextFocusRequester = webUrlFocusRequester
        )
        FormField(
            value = webUrl,
            onValueChange = model::setWebUrl,
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
                onValueChange = model::setLatitude,
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
                onValueChange = model::setLongitude,
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
            dialog = {
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
                        model.setPoints(
                            points.toMutableList().apply {
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
                                model.setPoints(
                                    points.toMutableList().apply {
                                        remove(point)
                                    }
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
}
