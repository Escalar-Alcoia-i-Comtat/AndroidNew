package org.escalaralcoiaicomtat.android.viewmodel.editor

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.dao.deleteRecursively
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.utils.UriUtils.getFileName
import org.escalaralcoiaicomtat.android.utils.compat.BitmapCompat
import org.escalaralcoiaicomtat.android.utils.letIf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.UUID

/**
 * Provides a ViewModel for interacting with the [EditorActivity]. All parent types must run
 * [onInit] when initialized:
 * ```kotlin
 * init {
 *   onInit()
 * }
 * ```
 */
@Suppress("TooManyFunctions")
abstract class EditorModel<
        ParentType : BaseEntity?,
        ElementType : BaseEntity,
        ChildrenType : BaseEntity?,
        UiStateClass : EditorModel.BaseUiState
        >(
    application: Application,
    protected val parentId: Long?,
    protected val elementId: Long?,
    defaultStateClass: () -> UiStateClass
) : AndroidViewModel(application), LifecycleOwner {
    @Suppress("LeakingThis")
    @SuppressLint("StaticFieldLeak")
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val database = AppDatabase.getInstance(application)
    protected val dao = database.dataDao()

    protected abstract val elementSerializer: JsonSerializer<ElementType>

    /**
     * The endpoint to make the POST request to. Options:
     * - `area`
     * - `zone`
     * - `sector`
     * - `path`
     */
    protected abstract val creatorEndpoint: String

    protected val _uiState = MutableStateFlow(defaultStateClass())
    val uiState = _uiState.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), defaultStateClass())

    /**
     * Used together with [BaseUiState.kmzName] to hold the currently selected KMZ file if any. This
     * variable holds the contents of the KMZ file selected.
     */
    private var kmzData: ByteArray? = null

    /**
     * Used together with [BaseUiState.gpxName] to hold the currently selected GPX file if any. This
     * variable holds the contents of the GPX file selected.
     */
    private var gpxData: ByteArray? = null

    /**
     * Whether the image is being loaded from the filesystem after being selected.
     */
    private val _isLoadingImage = MutableStateFlow(false)

    /**
     * Whether the image is being loaded from the filesystem after being selected.
     */
    val isLoadingImage = _isLoadingImage.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Will be not be null while the object is being created, contains the progress of upload.
     * `-1` for indeterminate.
     */
    private val _isCreating = MutableStateFlow<CreationStep?>(null)

    /**
     * Will be not be null while the object is being created, contains the progress of upload.
     * `-1` for indeterminate.
     */
    val isCreating = _isCreating.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Stores whether a process of deletion is being performed. This is used by the deletion
     * dialog to block the user interaction until the operation is complete.
     */
    private val _isDeleting = MutableStateFlow(false)

    /**
     * Stores whether a process of deletion is being performed. This is used by the deletion
     * dialog to block the user interaction until the operation is complete.
     */
    val isDeleting = _isDeleting.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /**
     * Whether the form is completely filled or not. The creation button should be disabled if
     * this is not true.
     */
    val isFilled: StateFlow<Boolean> = uiState
        .map { checkRequirements(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    abstract val hasParent: Boolean

    private val _parent = MutableStateFlow<ParentType?>(null)
    val parent: StateFlow<ParentType?> = _parent.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _element = MutableStateFlow<ElementType?>(null)
    val element: StateFlow<ElementType?> = _element.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * If the server returns an error while performing an operation, it should be passed here,
     * and the UI will be updated.
     */
    private val _requestException = MutableStateFlow<Exception?>(null)

    /**
     * If the server returns an error while performing an operation, it should be passed here,
     * and the UI will be updated.
     */
    val requestException = _requestException.asStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * For appending all the data required for creating the object in the server. Will be sent
     * to the creation endpoint. Automatically appends the data in [BaseUiState.image] when it's not null.
     */
    protected abstract fun FormBuilder.getFormData(state: UiStateClass, element: ElementType?)

    protected abstract val whenNotFound: (suspend () -> Unit)?

    open suspend fun init(parent: ParentType) {}

    /**
     * Called when edit is requested, and the child has been found
     */
    open suspend fun fill(child: ElementType) {}

    open suspend fun fetchParent(parentId: Long): ParentType? = null

    open suspend fun fetchChild(childId: Long): ElementType? = null

    /**
     * Can be used for preparing data to send in the form asynchronously. For example,
     * compressing files, or doing heavy calculations.
     */
    open suspend fun prepareData(): FormBuilder.() -> Unit = {}

    /**
     * Should use [dao] to insert the given [element] into the database.
     */
    protected abstract suspend fun insert(element: ElementType)

    /**
     * Should use [dao] to update the given [element].
     */
    protected abstract suspend fun update(element: ElementType)

    fun onInit() {
        Timber.d("Initializing ${this::class.simpleName}...")
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        viewModelScope.launch(Dispatchers.IO) {
            if (!hasParent) {
                // If fetch parent is null, type doesn't have parent
                Timber.d("${this@EditorModel::class.simpleName} doesn't have a parent.")
            } else {
                Timber.d("Fetching parent with id $parentId...")
                val parent = fetchParent(parentId!!)
                if (parent == null) {
                    Timber.e("Could not get a valid parent with id $parentId")
                    whenNotFound?.invoke()
                    return@launch
                }
                Timber.d("Parent loaded, posting value...")
                _parent.emit(parent)

                val parentName = parent.let { it::class.simpleName }
                CoroutineScope(Dispatchers.IO).launch {
                    Timber.d("Initializing data for $parentName #$parentId")
                    init(parent)
                }
            }

            if (elementId != null) {
                val child = fetchChild(elementId)
                if (child != null) {
                    Timber.d("Loaded element's data. Posting and filling...")
                    _element.emit(child)

                    setImageUUID((child as? ImageEntity?)?.imageUUID)
                    (child as? ImageEntity?)?.let(ImageEntity::imageUUID).let(::setImageUUID)

                    fill(child)
                } else {
                    Timber.w("Tried to load a non-existing child. ID: $elementId")
                    whenNotFound?.invoke()
                }
            } else {
                Timber.d("Won't load element since elementId is null")
            }

            withContext(Dispatchers.Main) {
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }
        }
    }

    override fun onCleared() {
        uiState.value.recycleImage()
        Timber.d("Cleared View Model.")
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun create() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            _isCreating.emit(CreationStep.Compressing)

            val uiState = uiState.value
            val element = element.value

            val originalUUID = (element as? ImageEntity?)?.imageUUID
            val currentImageUUID = uiState.imageUUID

            val imageBytes = if (
            // if creating new area
                originalUUID == null ||
                // or image updated
                currentImageUUID == null ||
                originalUUID != currentImageUUID
            ) {
                uiState.image?.let { image ->
                    Timber.d("Compressing image...")
                    ByteArrayOutputStream()
                        .apply {
                            image.compress(BitmapCompat.WEBP_LOSSLESS, 100, this)
                        }
                        .toByteArray()
                }
            } else {
                null
            }

            val extraData = prepareData()

            Timber.d("Sending data to server...")
            val response = ktorHttpClient.submitFormWithBinaryData(
                EndpointUtils.getUrl(creatorEndpoint).letIf(elementId != null) {
                    "$it/$elementId"
                },
                formData = formData {
                    imageBytes?.let { bytes ->
                        append("image", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/webp")
                            append(HttpHeaders.ContentDisposition, "filename=image.webp")
                        })
                    }
                    kmzData?.let { bytes ->
                        append("kmz", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "application/vnd.google-earth.kmz")
                            append(HttpHeaders.ContentDisposition, "filename=track.kmz")
                        })
                    }
                    gpxData?.let { bytes ->
                        append("gpx", bytes, Headers.build {
                            append(HttpHeaders.ContentType, "application/gpx+xml")
                            append(HttpHeaders.ContentDisposition, "filename=track.gpx")
                        })
                    }

                    extraData()
                    getFormData(uiState, element)
                }
            ) {
                val apiKey = Preferences.getApiKey(getApplication()).firstOrNull()
                header(HttpHeaders.Authorization, "Bearer $apiKey")

                onUpload { bytesSentTotal, contentLength ->
                    contentLength ?: return@onUpload
                    _isCreating.emit(
                        CreationStep.Uploading((bytesSentTotal.toDouble() / contentLength).toFloat())
                    )
                }
            }
            val bodyStr = response.bodyAsText()
            try {
                val status = response.status
                val body = JSONObject(bodyStr)

                val operation: suspend (element: ElementType) -> Unit = when (status) {
                    HttpStatusCode.Created -> {
                        Timber.i("Creation complete (status=$status). Inserting into database...")
                        ::insert
                    }

                    HttpStatusCode.OK -> {
                        Timber.i("Update complete (status=$status). Updating database...")
                        ::update
                    }

                    else -> throw RequestException(status, body)
                }
                _isCreating.emit(CreationStep.Finishing)

                val data = body.getJSONObject("data")
                val elementJson = data.getJSONObject("element")
                val newElement = elementSerializer.fromJson(elementJson)

                if (newElement is ImageEntity) {
                    newElement.updateImageIfNeeded(getApplication())
                }

                operation(newElement)
            } catch (e: RequestException) {
                _requestException.emit(e)
            } catch (e: JSONException) {
                Timber.e(e, "Could not parse JSON.\n\tBody: $bodyStr")
                _requestException.emit(e)
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Timber.e(e, "Could not create or update.")
                _requestException.emit(e)
            }

            _isCreating.emit(null)
        }
    }

    /**
     * Starts deleting [element] and all of its children. Updates [isDeleting] to `true` at
     * start, and to `false` at end.
     */
    fun delete() = viewModelScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            try {
                _isDeleting.emit(true)

                val element = element.value
                if (element == null) {
                    Timber.w("Won't delete anything since element is null.")
                } else {
                    Timber.i("Deleting $element recursively.")
                    dao.deleteRecursively(element)
                }
            } finally {
                _isDeleting.emit(false)
            }
        }
    }


    /**
     * Checks if the requirements for creating the object are met. This is used to enable or
     * disable the creation button.
     * @return `true` if the requirements are met, `false` otherwise.
     */
    protected abstract fun checkRequirements(state: UiStateClass): Boolean


    fun loadImage(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        _isLoadingImage.emit(true)
        getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
            setImage(bitmap, null)
        }
        _isLoadingImage.emit(false)
    }

    private fun loadFile(
        uri: Uri,
        nameSetter: (String?) -> Unit,
        onDataLoaded: (ByteArray) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        val fileName = getApplication<Application>().getFileName(uri)
        if (fileName == null) Timber.w("Could not get name for file at $uri.")
        nameSetter(fileName)

        getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pdf ->
            FileInputStream(pdf.fileDescriptor).use { stream ->
                val bytes = stream.readBytes()
                onDataLoaded(bytes)
            }
        }
    }


    fun loadKmz(uri: Uri) = loadFile(uri, ::setKmzName) {
        Timber.d("Picked new KMZ file: ${uiState.value.kmzName}. Size: ${it.size}")
        kmzData = it
    }

    fun loadGpx(uri: Uri) = loadFile(uri, ::setGpxName) {
        Timber.d("Picked new GPX file: ${uiState.value.gpxName}. Size: ${it.size}")
        gpxData = it
    }


    fun dismissServerError() {
        _requestException.tryEmit(null)
    }

    fun setImage(image: Bitmap, uuid: UUID?) {
        @Suppress("UNCHECKED_CAST")
        _uiState.tryEmit(
            uiState.value.copy(image = image, imageUUID = uuid) as UiStateClass
        )
    }

    fun setImageUUID(uuid: UUID?) {
        @Suppress("UNCHECKED_CAST")
        _uiState.tryEmit(
            uiState.value.copy(imageUUID = uuid) as UiStateClass
        )
    }

    protected fun setKmzName(name: String?) {
        @Suppress("UNCHECKED_CAST")
        _uiState.tryEmit(
            uiState.value.copy(kmzName = name) as UiStateClass
        )
    }

    protected fun setGpxName(name: String?) {
        @Suppress("UNCHECKED_CAST")
        _uiState.tryEmit(
            uiState.value.copy(gpxName = name) as UiStateClass
        )
    }


    open class CreationStep(@StringRes val messageRes: Int) {
        object Compressing : CreationStep(R.string.creation_step_compressing)

        @Suppress("MagicNumber")
        class Uploading(progress: Float) : ProgressStep(
            R.string.creation_step_uploading,
            progress * 100
        )

        object Finishing : CreationStep(R.string.creation_step_finishing)

        open fun getString(context: Context) = context.getString(messageRes)
    }

    open class ProgressStep(
        @StringRes messageRes: Int,
        val progress: Float
    ) : CreationStep(messageRes) {
        override fun getString(context: Context): String =
            context.getString(messageRes, progress)
    }

    abstract class BaseUiState(
        open val image: Bitmap? = null,
        open val imageUUID: UUID? = null,
        open val kmzName: String? = null,
        open val gpxName: String? = null
    ) {
        abstract fun copy(
            image: Bitmap? = this.image,
            imageUUID: UUID? = this.imageUUID,
            kmzName: String? = this.kmzName,
            gpxName: String? = this.gpxName
        ): BaseUiState

        fun recycleImage() {
            image?.recycle()
        }
    }
}
