package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.UiThread
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.ktor.client.request.forms.FormBuilder
import org.burnoutcrew.reorderable.ItemPosition
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.type.Builder
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.PitchInfo
import org.escalaralcoiaicomtat.android.utils.appendDifference
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import timber.log.Timber

class PathModel(
    application: Application,
    sectorId: Long,
    pathId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<Sector, Path, BaseEntity>(application, sectorId, pathId) {
    companion object {
        fun Factory(
            sectorId: Long,
            pathId: Long?,
            whenSectorNotFound: () -> Unit
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                PathModel(application, sectorId, pathId, whenSectorNotFound)
            }
        }
    }

    override val elementSerializer: JsonSerializer<Path> = Path.Companion

    override val creatorEndpoint: String = "path"

    val displayName = MutableLiveData<String>()
    val sketchId = MutableLiveData<String>()

    val height = MutableLiveData<String?>()
    val grade = MutableLiveData<GradeValue?>()
    val ending = MutableLiveData<Ending?>()

    val pitches = MutableLiveData<List<PitchInfo>?>()

    val stringCount = MutableLiveData<String?>()

    val paraboltCount = MutableLiveData<String?>()
    val burilCount = MutableLiveData<String?>()
    val pitonCount = MutableLiveData<String?>()
    val spitCount = MutableLiveData<String?>()
    val tensorCount = MutableLiveData<String?>()

    val crackerRequired = MutableLiveData(false)
    val friendRequired = MutableLiveData(false)
    val lanyardRequired = MutableLiveData(false)
    val nailRequired = MutableLiveData(false)
    val pitonRequired = MutableLiveData(false)
    val stapesRequired = MutableLiveData(false)

    val showDescription = MutableLiveData(false)
    val description = MutableLiveData<String?>()

    val builder = MutableLiveData<Builder?>()
    val reBuilders = MutableLiveData<List<Builder>?>()

    /**
     * Used for displaying the current sector's image in the side of the screen.
     */
    val sectorImage = MutableLiveData<Bitmap?>(null)

    override val hasParent: Boolean = true

    init {
        onInit()
    }

    override suspend fun init(parent: Sector) {
        // Load sector image
        parent.readImageFile(getApplication(), lifecycle).collect {
            val bitmap: Bitmap? = it?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            sectorImage.postValue(bitmap)
        }
    }

    override suspend fun fill(child: Path) {
        displayName.postValue(child.displayName)
        sketchId.postValue(child.sketchId.toString())

        height.postValue(child.height?.toString())
        grade.postValue(child.grade)
        ending.postValue(child.ending)

        pitches.postValue(child.pitches)

        stringCount.postValue(child.stringCount?.toString())

        paraboltCount.postValue(child.paraboltCount?.toString())
        burilCount.postValue(child.burilCount?.toString())
        pitonCount.postValue(child.pitonCount?.toString())
        spitCount.postValue(child.spitCount?.toString())
        tensorCount.postValue(child.tensorCount?.toString())

        crackerRequired.postValue(child.crackerRequired)
        friendRequired.postValue(child.friendRequired)
        lanyardRequired.postValue(child.lanyardRequired)
        nailRequired.postValue(child.nailRequired)
        pitonRequired.postValue(child.pitonRequired)
        stapesRequired.postValue(child.stapesRequired)

        showDescription.postValue(child.showDescription)
        description.postValue(child.description)

        builder.postValue(child.builder)
        reBuilders.postValue(child.reBuilder)
    }

    private fun checkRequirements(): Boolean {
        return displayName.value != null && sketchId.value != null
    }

    override val isFilled: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(displayName) { value = checkRequirements() }
        addSource(sketchId) { value = checkRequirements() }
    }

    override suspend fun fetchParent(parentId: Long): Sector? = dao.getSector(parentId)

    override suspend fun fetchChild(childId: Long): Path? = dao.getPath(childId)

    override fun FormBuilder.getFormData() {
        Timber.i("Creating a new path for sector #$parentId")

        appendDifference("displayName", displayName.value, element.value?.displayName)
        appendDifference("sketchId", sketchId.value, element.value?.sketchId)

        appendDifference("height", height.value?.toULongOrNull(), element.value?.height)
        appendDifference("grade", grade.value, element.value?.grade)
        appendDifference("ending", ending.value, element.value?.ending)

        appendDifference("pitches", pitches.value, element.value?.pitches)

        appendDifference("stringCount", stringCount.value, element.value?.stringCount)

        appendDifference("paraboltCount", paraboltCount.value, element.value?.paraboltCount)
        appendDifference("burilCount", burilCount.value, element.value?.burilCount)
        appendDifference("pitonCount", pitonCount.value, element.value?.pitonCount)
        appendDifference("spitCount", spitCount.value, element.value?.spitCount)
        appendDifference("tensorCount", tensorCount.value, element.value?.tensorCount)

        appendDifference(
            "crackerRequired",
            crackerRequired.value,
            element.value?.crackerRequired
        )
        appendDifference("friendRequired", friendRequired.value, element.value?.friendRequired)
        appendDifference(
            "lanyardRequired",
            lanyardRequired.value,
            element.value?.lanyardRequired
        )
        appendDifference("nailRequired", nailRequired.value, element.value?.nailRequired)
        appendDifference("pitonRequired", pitonRequired.value, element.value?.pitonRequired)
        appendDifference("stapesRequired", stapesRequired.value, element.value?.stapesRequired)

        appendDifference(
            "showDescription",
            showDescription.value,
            element.value?.showDescription
        )
        appendDifference(
            "description",
            description.value?.takeIf { it.isNotBlank() },
            element.value?.description
        )

        appendDifference("builder", builder.value, element.value?.builder)
        appendDifference("reBuilders", reBuilders.value, element.value?.reBuilder)

        append("sector", parentId!!)
    }

    @UiThread
    fun <T> modifyPitch(
        index: Int,
        value: T,
        property: (PitchInfo) -> T,
        copy: (item: PitchInfo, value: T?) -> PitchInfo
    ) {
        synchronized(pitches) {
            val list = (pitches.value ?: emptyList()).toMutableList()
            val item = list.removeAt(index)
            list.add(
                index,
                if (property(item) == value)
                    copy(item, null)
                else
                    copy(item, value)
            )
            pitches.postValue(list)
        }
    }

    fun movePitch(from: ItemPosition, to: ItemPosition) {
        try {
            Timber.d("Moving pitch from ${from.index} to ${to.index}")
            synchronized(pitches) {
                val list = (pitches.value ?: emptyList()).toMutableList()
                list.add(to.index, list.removeAt(from.index))
                pitches.postValue(list)
            }
        } catch (_: IndexOutOfBoundsException) {
            Timber.w("Could not move pitch from ${from.index} to ${to.index}: Out of bounds")
        }
    }

    override suspend fun insert(element: Path) {
        dao.insert(element)
    }

    override suspend fun update(element: Path) = dao.update(element)
}
