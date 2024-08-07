package org.escalaralcoiaicomtat.android.activity.creation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.ui.form.FormCheckbox
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormGPXPicker
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.ui.form.FormSegmentedButton
import org.escalaralcoiaicomtat.android.ui.form.SizeMode
import org.escalaralcoiaicomtat.android.viewmodel.editor.SectorModel

class NewSectorActivity : EditorActivity<Zone, Sector, Path, SectorModel>(
    createTitleRes = R.string.new_sector_title,
    editTitleRes = R.string.edit_sector_title
) {

    object Contract : ResultContract<NewSectorActivity>(NewSectorActivity::class)

    override val model: SectorModel by viewModels {
        SectorModel.Factory(parentId!!, elementId, ::onBack)
    }

    @Composable
    override fun ColumnScope.Editor(parent: Zone?) {
        val uiState by model.uiState.collectAsState()
        val displayName = uiState.displayName
        val image = uiState.image
        val gpxFile = uiState.gpxName
        val latitude = uiState.latitude
        val longitude = uiState.longitude
        val kidsApt = uiState.kidsApt
        val walkingTime = uiState.walkingTime

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
            modifier = Modifier.fillMaxWidth()
        )

        FormImagePicker(image, contentDescription = displayName, model.isLoadingImage) {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        FormGPXPicker(fileName = gpxFile) {
            gpxPicker.launch(arrayOf("*/*"))
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

        FormSegmentedButton(
            items = SunTime.entries.map { stringResource(it.label) },
            label = stringResource(R.string.form_sun_time),
            modifier = Modifier.padding(top = 8.dp),
            mode = SizeMode.FILL_MAX_WIDTH,
            onItemSelection = { SunTime.entries[it].let(model::setSunTime) }
        )

        FormCheckbox(
            checked = kidsApt,
            onCheckedChange = model::setKidsApt,
            label = stringResource(R.string.form_kids_apt)
        )

        FormField(
            value = walkingTime,
            onValueChange = model::setWalkingTime,
            label = stringResource(R.string.form_walking_time),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number
        )
    }
}
