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
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer

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

    override val isFilled = MediatorLiveData<Boolean>()
        .apply {
            addSource(displayName) { value = checkRequirements() }
            addSource(webUrl) { value = checkRequirements() }
            addSource(image) { value = checkRequirements() }
        }
        .asFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

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
