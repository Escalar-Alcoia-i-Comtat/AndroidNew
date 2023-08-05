package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker

class NewAreaActivity : CreatorActivity<NewAreaActivity.Model>(R.string.new_area_title) {
    object Contract : ActivityResultContract<Void?, Boolean>() {
        override fun createIntent(context: Context, input: Void?): Intent =
            Intent(context, NewAreaActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            resultCode == Activity.RESULT_OK
    }

    override val model: Model by viewModels()

    @Composable
    override fun ColumnScope.Content() {
        val displayName by model.displayName.observeAsState(initial = "")
        val webUrl by model.webUrl.observeAsState(initial = "")
        val image by model.image.observeAsState()

        val webUrlFocusRequester = remember { FocusRequester() }

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
            imagePicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    }

    class Model(application: Application) : CreatorModel(application) {
        override val creatorEndpoint: String = "area"

        val displayName = MutableLiveData("")
        val webUrl = MutableLiveData("")

        override val isFilled = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = it.isNotBlank() }
            addSource(webUrl) { value = it.isNotBlank() }
            addSource(image) { value = it != null }
        }

        override fun FormBuilder.getFormData() {
            append("displayName", displayName.value!!)
            append("webUrl", webUrl.value!!)
        }
    }
}
