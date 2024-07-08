package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.flow.update
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import java.util.UUID

class ZoneModel(
    application: Application,
    areaId: Long,
    zoneId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<Area, Zone, Sector, ZoneModel.UiState>(application, areaId, zoneId, { UiState() }) {
    companion object {
        fun Factory(
            areaId: Long,
            zoneId: Long?,
            whenNotFound: () -> Unit
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                ZoneModel(application, areaId, zoneId, whenNotFound)
            }
        }
    }

    override val elementSerializer: JsonSerializer<Zone> = Zone.Companion

    override val creatorEndpoint: String = "zone"

    override val hasParent: Boolean = true

    init {
        onInit()
    }

    override fun checkRequirements(state: UiState): Boolean {
        return state.displayName.isNotBlank() &&
                state.webUrl.isNotBlank() &&
                state.image != null &&
                state.kmzName != null &&
                state.latitude.toDoubleOrNull() != null &&
                state.longitude.toDoubleOrNull() != null
    }

    override suspend fun fill(child: Zone) {
        _uiState.emit(
            UiState(
                displayName = child.displayName,
                webUrl = child.webUrl.toString(),
                latitude = child.point?.latitude?.toString() ?: "",
                longitude = child.point?.longitude?.toString() ?: "",
                points = child.points,
                kmzName = child.kmz
            )
        )

        child.readImageFile(getApplication(), lifecycle).collect { file ->
            file?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                ?.let { setImage(it, null) }
        }
    }

    override suspend fun fetchParent(parentId: Long): Area? = dao.getArea(parentId)

    override suspend fun fetchChild(childId: Long): Zone? = dao.getZone(childId)

    override fun FormBuilder.getFormData(state: UiState, element: Zone?) {
        appendDifference("displayName", state.displayName, element?.displayName)
        appendDifference("webUrl", state.webUrl, element?.webUrl)
        appendDifference(
            "point",
            LatLng(state.latitude.toDouble(), state.longitude.toDouble()),
            element?.point
        )
        appendDifference("points", state.points, element?.points ?: emptyList())
    }

    override suspend fun insert(element: Zone) { dao.insert(element) }

    override suspend fun update(element: Zone) = dao.update(element)


    fun setDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    fun setWebUrl(webUrl: String) {
        _uiState.update { it.copy(webUrl = webUrl) }
    }

    fun setLatitude(latitude: String) {
        _uiState.update { it.copy(latitude = latitude) }
    }

    fun setLongitude(longitude: String) {
        _uiState.update { it.copy(longitude = longitude) }
    }

    fun setPoints(points: List<DataPoint>) {
        _uiState.update { it.copy(points = points) }
    }


    data class UiState(
        val displayName: String = "",
        val webUrl: String = "",
        val latitude: String = "",
        val longitude: String = "",
        val points: List<DataPoint> = emptyList(),
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
            latitude = latitude,
            longitude = longitude,
            points = points,
            image = image,
            imageUUID = imageUUID,
            kmzName = kmzName,
            gpxName = gpxName
        )
    }
}
