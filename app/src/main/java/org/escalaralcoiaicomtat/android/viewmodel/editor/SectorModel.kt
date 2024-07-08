package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.flow.update
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import timber.log.Timber
import java.util.UUID

class SectorModel(
    application: Application,
    zoneId: Long,
    sectorId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<Zone, Sector, Path, SectorModel.UiState>(
    application,
    zoneId,
    sectorId,
    { UiState() }) {
    companion object {
        fun Factory(
            zoneId: Long,
            sectorId: Long?,
            whenNotFound: () -> Unit
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                SectorModel(application, zoneId, sectorId, whenNotFound)
            }
        }
    }

    override val elementSerializer: JsonSerializer<Sector> = Sector.Companion

    override val creatorEndpoint: String = "sector"

    override val hasParent: Boolean = true

    init {
        onInit()
    }

    override fun checkRequirements(state: UiState): Boolean {
        return state.displayName.isNotBlank() &&
                state.image != null &&
                state.latitude.toDoubleOrNull() != null &&
                state.longitude.toDoubleOrNull() != null
    }

    override suspend fun fill(child: Sector) {
        _uiState.emit(
            UiState(
                displayName = child.displayName,
                kidsApt = child.kidsApt,
                sunTime = child.sunTime,
                walkingTime = child.walkingTime?.toString() ?: "",
                latitude = child.point?.latitude?.toString() ?: "",
                longitude = child.point?.longitude?.toString() ?: "",
                weight = child.weight
            )
        )

        child.readImageFile(getApplication(), lifecycle).collect { file ->
            file?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                ?.let { setImage(it, null) }
        }
        child.readGpxFile(getApplication(), lifecycle).collect {
            setGpxName(it?.let { String(it) })
        }
    }

    override suspend fun fetchParent(parentId: Long): Zone? = dao.getZone(parentId)

    override suspend fun fetchChild(childId: Long): Sector? = dao.getSector(childId)

    override fun FormBuilder.getFormData(state: UiState, element: Sector?) {
        Timber.i("Creating a new sector for zone #$parentId")

        appendDifference("displayName", state.displayName, element?.displayName)
        appendDifference("point", LatLng(state.latitude, state.longitude), element?.point)
        appendDifference("kidsApt", state.kidsApt, element?.kidsApt)
        appendDifference("sunTime", state.sunTime, element?.sunTime)
        appendDifference(
            "walkingTime",
            state.walkingTime.takeIf { it.isNotBlank() }?.toLongOrNull(),
            element?.walkingTime
        )
    }

    override suspend fun insert(element: Sector) {
        dao.insert(element)
    }

    override suspend fun update(element: Sector) = dao.update(element)


    fun setDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    fun setKidsApt(kidsApt: Boolean) {
        _uiState.update { it.copy(kidsApt = kidsApt) }
    }

    fun setSunTime(sunTime: SunTime) {
        _uiState.update { it.copy(sunTime = sunTime) }
    }

    fun setWalkingTime(walkingTime: String) {
        _uiState.update { it.copy(walkingTime = walkingTime) }
    }

    fun setLatitude(latitude: String) {
        _uiState.update { it.copy(latitude = latitude) }
    }

    fun setLongitude(longitude: String) {
        _uiState.update { it.copy(longitude = longitude) }
    }

    fun setWeight(weight: String) {
        _uiState.update { it.copy(weight = weight) }
    }


    data class UiState(
        val displayName: String = "",
        val kidsApt: Boolean = false,
        val sunTime: SunTime = SunTime.None,
        val walkingTime: String = "",
        val latitude: String = "",
        val longitude: String = "",
        val weight: String = "",
        override val image: Bitmap? = null,
        override val imageUUID: UUID? = null,
        override val kmzName: String? = null,
        override val gpxName: String? = null
    ) : BaseUiState(image, imageUUID, kmzName, gpxName) {
        override fun copy(
            image: Bitmap?,
            imageUUID: UUID?,
            kmzName: String?,
            gpxName: String?
        ): BaseUiState = copy(
            displayName = displayName,
            kidsApt = kidsApt,
            sunTime = sunTime,
            walkingTime = walkingTime,
            latitude = latitude,
            longitude = longitude,
            weight = weight,
            image = image,
            imageUUID = imageUUID,
            kmzName = kmzName,
            gpxName = gpxName
        )
    }
}
