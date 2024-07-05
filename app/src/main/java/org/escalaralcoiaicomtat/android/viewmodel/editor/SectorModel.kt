package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import timber.log.Timber

class SectorModel(
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
                SectorModel(application, zoneId, sectorId, whenNotFound)
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

    override val isFilled = MediatorLiveData<Boolean>()
        .apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
            addSource(latitude) { value = checkRequirements() }
            addSource(longitude) { value = checkRequirements() }
        }
        .asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
        child.readGpxFile(getApplication(), lifecycle).collect {
            gpxName.postValue(it?.let { String(it) })
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
