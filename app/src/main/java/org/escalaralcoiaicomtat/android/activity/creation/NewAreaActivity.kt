package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.form.FormImagePicker
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer

class NewAreaActivity : EditorActivity<BaseEntity, Area, Zone, NewAreaActivity.AreaModel>(
    createTitleRes = R.string.new_area_title,
    editTitleRes = R.string.edit_area_title
) {
    object Contract : ActivityResultContract<Area?, Result>() {
        override fun createIntent(context: Context, input: Area?): Intent =
            Intent(context, NewAreaActivity::class.java).apply {
                putExtra(EXTRA_ELEMENT_ID, input?.id)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Result =
            when (resultCode) {
                RESULT_CREATE_OK -> Result.CreateSuccess
                RESULT_EDIT_OK -> Result.EditSuccess
                RESULT_CREATE_CANCELLED -> Result.CreateCancelled
                RESULT_EDIT_CANCELLED -> Result.EditCancelled
                RESULT_DELETE_OK -> Result.Deleted
                else -> {
                    val throwable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent?.getSerializableExtra(RESULT_EXCEPTION, Throwable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent?.getSerializableExtra(RESULT_EXCEPTION) as Throwable
                    }
                    Result.Failure(throwable)
                }
            }
    }

    override val model: AreaModel by viewModels { AreaModel.Factory(elementId, ::onBack) }

    @Composable
    override fun ColumnScope.Editor(parent: BaseEntity?) {
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

    class AreaModel(
        application: Application,
        areaId: Long?,
        override val whenNotFound: suspend () -> Unit
    ) : EditorModel<BaseEntity, Area, Zone>(application, null, areaId) {
        companion object {
            fun Factory(
                areaId: Long?,
                whenNotFound: () -> Unit
            ): ViewModelProvider.Factory = viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                    AreaModel(application, areaId, whenNotFound)
                }
            }
        }

        override val elementSerializer: JsonSerializer<Area> = Area.CREATOR

        override val creatorEndpoint: String = "area"

        override val hasParent: Boolean = false

        val displayName = MutableLiveData("")
        val webUrl = MutableLiveData("")

        init {
            onInit()
        }

        private fun checkRequirements(): Boolean =
            displayName.value?.isNotBlank() == true &&
                webUrl.value?.isNotBlank() == true &&
                image.value != null

        override val isFilled = MediatorLiveData<Boolean>().apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(webUrl) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
        }

        override suspend fun fill(child: Area) {
            displayName.postValue(child.displayName)
            webUrl.postValue(child.webUrl.toString())
            child.readImageFile(getApplication(), lifecycle).collect {
                val bitmap: Bitmap? = it?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                image.postValue(bitmap)
            }
        }

        override suspend fun fetchChild(childId: Long): Area? = dao.getArea(childId)

        override fun FormBuilder.getFormData() {
            appendDifference("displayName", displayName.value, element.value?.displayName)
            appendDifference("webUrl", webUrl.value, element.value?.webUrl)
        }

        override suspend fun insert(element: Area) { dao.insert(element) }

        override suspend fun update(element: Area) = dao.update(element)
    }
}
