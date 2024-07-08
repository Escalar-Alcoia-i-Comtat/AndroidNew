package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.flow.update
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import java.util.UUID

class AreaModel(
    application: Application,
    areaId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<BaseEntity, Area, Zone, AreaModel.UiState>(
    application,
    null,
    areaId,
    { UiState() }
) {
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

    init {
        onInit()
    }

    override fun checkRequirements(state: UiState): Boolean =
        state.let {
            state.displayName.isNotBlank() && state.webUrl.isNotBlank() && state.image != null
        }

    override suspend fun fill(child: Area) {
        _uiState.emit(
            UiState(
                displayName = child.displayName,
                webUrl = child.webUrl.toString()
            )
        )
        child.readImageFile(getApplication(), lifecycle).collect { file ->
            file?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                ?.let { setImage(it, null) }
        }
    }

    override suspend fun fetchChild(childId: Long): Area? = dao.getArea(childId)

    override fun FormBuilder.getFormData(state: UiState, element: Area?) {
        appendDifference("displayName", state.displayName, element?.displayName)
        appendDifference("webUrl", state.webUrl, element?.webUrl)
    }

    override suspend fun insert(element: Area) { dao.insert(element) }

    override suspend fun update(element: Area) = dao.update(element)


    fun setDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    fun setWebUrl(webUrl: String) {
        _uiState.update { it.copy(webUrl = webUrl) }
    }


    data class UiState(
        val displayName: String = "",
        val webUrl: String = "",
        override val image: Bitmap? = null,
        override val imageUUID: UUID? = null,
        override val kmzName: String? = null,
        override val gpxName: String? = null
    ): BaseUiState(image, imageUUID, kmzName, gpxName) {
        override fun copy(
            image: Bitmap?,
            imageUUID: UUID?,
            kmzName: String?,
            gpxName: String?
        ): BaseUiState = copy(
            displayName = displayName,
            webUrl = webUrl,
            image = image,
            imageUUID = imageUUID,
            kmzName = kmzName,
            gpxName = gpxName
        )
    }
}
