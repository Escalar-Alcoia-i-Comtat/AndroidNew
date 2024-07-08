package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.UiThread
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
import org.escalaralcoiaicomtat.android.utils.takeIfNotBlank
import timber.log.Timber
import java.util.UUID

@Suppress("TooManyFunctions")
class PathModel(
    application: Application,
    sectorId: Long,
    pathId: Long?,
    override val whenNotFound: suspend () -> Unit
) : EditorModel<Sector, Path, BaseEntity, PathModel.UiState>(
    application,
    sectorId,
    pathId,
    { UiState() }
) {
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
        setDisplayName(child.displayName)
        setSketchId(child.sketchId.toString())

        setHeight(child.height?.toString())
        setGrade(child.grade)
        setEnding(child.ending)

        setPitches(child.pitches)

        setStringCount(child.stringCount?.toString())

        setParaboltCount(child.paraboltCount?.toString())
        setBurilCount(child.burilCount?.toString())
        setPitonCount(child.pitonCount?.toString())
        setSpitCount(child.spitCount?.toString())
        setTensorCount(child.tensorCount?.toString())

        setCrackerRequired(child.crackerRequired)
        setFriendRequired(child.friendRequired)
        setLanyardRequired(child.lanyardRequired)
        setNailRequired(child.nailRequired)
        setPitonRequired(child.pitonRequired)
        setStapesRequired(child.stapesRequired)

        setShowDescription(child.showDescription)
        setDescription(child.description)

        setBuilder(child.builder)
        setReBuilders(child.reBuilder)
    }

    override fun checkRequirements(state: UiState): Boolean {
        return state.displayName.isNotBlank() && state.sketchId.isNotBlank()
    }

    fun setDisplayName(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value)
    }

    fun setSketchId(value: String) {
        _uiState.value = _uiState.value.copy(sketchId = value)
    }

    fun setHeight(value: String?) {
        _uiState.value = _uiState.value.copy(height = value?.takeIf { it.isNotBlank() })
    }

    fun setGrade(value: GradeValue?) {
        _uiState.value = _uiState.value.copy(grade = value)
    }

    fun toggleGrade(value: GradeValue) {
        _uiState.value = _uiState.value.copy(grade = if (uiState.value.grade == value) null else value)
    }

    fun setEnding(value: Ending?) {
        _uiState.value = _uiState.value.copy(ending = value)
    }

    fun toggleEnding(value: Ending) {
        _uiState.value = _uiState.value.copy(ending = if (uiState.value.ending == value) null else value)
    }

    fun setPitches(value: List<PitchInfo>?) {
        _uiState.value = _uiState.value.copy(pitches = value)
    }

    fun setStringCount(value: String?) {
        _uiState.value = _uiState.value.copy(stringCount = value?.takeIf { it.isNotBlank() })
    }

    fun setParaboltCount(value: String?) {
        _uiState.value = _uiState.value.copy(paraboltCount = value)
    }

    fun setBurilCount(value: String?) {
        _uiState.value = _uiState.value.copy(burilCount = value)
    }

    fun setPitonCount(value: String?) {
        _uiState.value = _uiState.value.copy(pitonCount = value)
    }

    fun setSpitCount(value: String?) {
        _uiState.value = _uiState.value.copy(spitCount = value)
    }

    fun setTensorCount(value: String?) {
        _uiState.value = _uiState.value.copy(tensorCount = value)
    }

    fun setCrackerRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(crackerRequired = value)
    }

    fun setFriendRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(friendRequired = value)
    }

    fun setLanyardRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(lanyardRequired = value)
    }

    fun setNailRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(nailRequired = value)
    }

    fun setPitonRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(pitonRequired = value)
    }

    fun setStapesRequired(value: Boolean) {
        _uiState.value = _uiState.value.copy(stapesRequired = value)
    }

    fun setShowDescription(value: Boolean) {
        _uiState.value = _uiState.value.copy(showDescription = value)
    }

    fun setDescription(value: String?) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun setBuilder(value: Builder?) {
        _uiState.value = _uiState.value.copy(builder = value)
    }

    fun setReBuilders(value: List<Builder>?) {
        _uiState.value = _uiState.value.copy(reBuilders = value)
    }

    override suspend fun fetchParent(parentId: Long): Sector? = dao.getSector(parentId)

    override suspend fun fetchChild(childId: Long): Path? = dao.getPath(childId)

    override fun FormBuilder.getFormData(state: UiState, element: Path?) {
        Timber.i("Creating a new path for sector #$parentId")

        appendDifference("displayName", state.displayName, element?.displayName)
        appendDifference("sketchId", state.sketchId.toLongOrNull(), element?.sketchId)

        appendDifference("height", state.height?.toLongOrNull(), element?.height)
        appendDifference("grade", state.grade, element?.grade)
        appendDifference("ending", state.ending, element?.ending)

        appendDifference("pitches", state.pitches, element?.pitches)

        appendDifference("stringCount", state.stringCount?.toLongOrNull(), element?.stringCount)

        appendDifference("paraboltCount", state.paraboltCount?.toLongOrNull(), element?.paraboltCount)
        appendDifference("burilCount", state.burilCount?.toLongOrNull(), element?.burilCount)
        appendDifference("pitonCount", state.pitonCount?.toLongOrNull(), element?.pitonCount)
        appendDifference("spitCount", state.spitCount?.toLongOrNull(), element?.spitCount)
        appendDifference("tensorCount", state.tensorCount?.toLongOrNull(), element?.tensorCount)

        appendDifference("crackerRequired", state.crackerRequired, element?.crackerRequired)
        appendDifference("friendRequired", state.friendRequired, element?.friendRequired)
        appendDifference("lanyardRequired", state.lanyardRequired, element?.lanyardRequired)
        appendDifference("nailRequired", state.nailRequired, element?.nailRequired)
        appendDifference("pitonRequired", state.pitonRequired, element?.pitonRequired)
        appendDifference("stapesRequired", state.stapesRequired, element?.stapesRequired)

        appendDifference("showDescription", state.showDescription, element?.showDescription)
        appendDifference("description", state.description.takeIfNotBlank(), element?.description)

        appendDifference("builder", state.builder, element?.builder)
        appendDifference("reBuilder", state.reBuilders, element?.reBuilder)
    }

    @UiThread
    fun <T> modifyPitch(
        index: Int,
        value: T,
        property: (PitchInfo) -> T,
        copy: (item: PitchInfo, value: T?) -> PitchInfo
    ) {
        synchronized(uiState) {
            val list = (uiState.value.pitches ?: emptyList()).toMutableList()
            val item = list.removeAt(index)
            list.add(
                index,
                if (property(item) == value)
                    copy(item, null)
                else
                    copy(item, value)
            )
            setPitches(list)
        }
    }

    fun movePitch(from: ItemPosition, to: ItemPosition) {
        try {
            Timber.d("Moving pitch from ${from.index} to ${to.index}")
            synchronized(uiState) {
                val list = (uiState.value.pitches ?: emptyList()).toMutableList()
                list.add(to.index, list.removeAt(from.index))
                setPitches(list)
            }
        } catch (_: IndexOutOfBoundsException) {
            Timber.w("Could not move pitch from ${from.index} to ${to.index}: Out of bounds")
        }
    }

    override suspend fun insert(element: Path) {
        dao.insert(element)
    }

    override suspend fun update(element: Path) = dao.update(element)


    data class UiState(
        val displayName: String = "",
        val sketchId: String = "",

        val height: String? = null,
        val grade: GradeValue? = null,
        val ending: Ending? = null,

        val pitches: List<PitchInfo>? = null,

        val stringCount: String? = null,

        val paraboltCount: String? = null,
        val burilCount: String? = null,
        val pitonCount: String? = null,
        val spitCount: String? = null,
        val tensorCount: String? = null,

        val crackerRequired: Boolean = false,
        val friendRequired: Boolean = false,
        val lanyardRequired: Boolean = false,
        val nailRequired: Boolean = false,
        val pitonRequired: Boolean = false,
        val stapesRequired: Boolean = false,

        val showDescription: Boolean = false,
        val description: String? = null,

        val builder: Builder? = null,
        val reBuilders: List<Builder>? = null,

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
            sketchId = sketchId,
            height = height,
            grade = grade,
            ending = ending,
            pitches = pitches,
            stringCount = stringCount,
            paraboltCount = paraboltCount,
            burilCount = burilCount,
            pitonCount = pitonCount,
            spitCount = spitCount,
            tensorCount = tensorCount,
            crackerRequired = crackerRequired,
            friendRequired = friendRequired,
            lanyardRequired = lanyardRequired,
            nailRequired = nailRequired,
            pitonRequired = pitonRequired,
            stapesRequired = stapesRequired,
            showDescription = showDescription,
            description = description,
            builder = builder,
            reBuilders = reBuilders,
            image = image,
            imageUUID = imageUUID,
            kmzName = kmzName,
            gpxName = gpxName
        )
    }
}
