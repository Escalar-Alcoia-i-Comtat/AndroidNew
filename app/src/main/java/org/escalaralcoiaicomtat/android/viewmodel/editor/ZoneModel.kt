package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.DataPoint
import org.escalaralcoiaicomtat.android.storage.type.LatLng
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer

class ZoneModel(
    application: Application,
    areaId: Long,
    zoneId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<Area, Zone, Sector>(application, areaId, zoneId) {
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

    val displayName = MutableLiveData("")
    val webUrl = MutableLiveData("")
    val latitude = MutableLiveData("")
    val longitude = MutableLiveData("")
    val points = MutableLiveData<List<DataPoint>>()

    init {
        onInit()
    }

    private fun checkRequirements(): Boolean {
        return displayName.value?.isNotBlank() == true &&
                webUrl.value?.isNotBlank() == true &&
                image.value != null &&
                kmzName.value != null &&
                latitude.value?.toDoubleOrNull() != null &&
                longitude.value?.toDoubleOrNull() != null
    }

    override val isFilled = MediatorLiveData<Boolean>()
        .apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(webUrl) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
            addSource(kmzName) { value = checkRequirements() }
            addSource(latitude) { value = checkRequirements() }
            addSource(longitude) { value = checkRequirements() }
            addSource(points) { value = checkRequirements() }
        }
        .asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    override suspend fun fill(child: Zone) {
        displayName.postValue(child.displayName)
        webUrl.postValue(child.webUrl.toString())
        latitude.postValue(child.point?.latitude?.toString())
        longitude.postValue(child.point?.longitude?.toString())
        points.postValue(child.points)
        kmzName.postValue(child.kmz)

        child.readImageFile(getApplication(), lifecycle).collect {
            val bitmap: Bitmap? = it?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            image.postValue(bitmap)
        }
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
        appendDifference(
            "points",
            points.value ?: emptyList(),
            element.value?.points ?: emptyList()
        )
        append("area", parentId!!)
    }

    override suspend fun insert(element: Zone) { dao.insert(element) }

    override suspend fun update(element: Zone) = dao.update(element)
}
