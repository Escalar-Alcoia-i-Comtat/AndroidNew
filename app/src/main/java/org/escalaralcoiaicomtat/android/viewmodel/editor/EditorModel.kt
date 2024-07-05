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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.bodyAsJson
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
abstract class EditorModel
<ParentType : BaseEntity?, ElementType : BaseEntity, ChildrenType : BaseEntity?>(
    application: Application,
    protected val parentId: Long?,
    protected val elementId: Long?
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

    /**
     * The image of the object to create, if any.
     */
    val image = MutableLiveData<Bitmap?>(null)

    /**
     * Stores the original imageUUID if any. Used for checking if image has been updated.
     */
    private val imageUUID = MutableLiveData<UUID?>(null)

    /**
     * Used together with [kmzData] to hold the currently selected KMZ file if any. This
     * variable stores the name of the KMZ file selected.
     */
    val kmzName = MutableLiveData<String?>(null)

    /**
     * Used together with [kmzName] to hold the currently selected KMZ file if any. This
     * variable holds the contents of the KMZ file selected.
     */
    private var kmzData: ByteArray? = null

    /**
     * Used together with [gpxData] to hold the currently selected GPX file if any. This
     * variable stores the name of the GPX file selected.
     */
    val gpxName = MutableLiveData<String?>(null)

    /**
     * Used together with [gpxName] to hold the currently selected GPX file if any. This
     * variable holds the contents of the GPX file selected.
     */
    private var gpxData: ByteArray? = null

    /**
     * Whether the image is being loaded from the filesystem after being selected.
     */
    val isLoadingImage = MutableLiveData(false)

    /**
     * Will be not be null while the object is being created, contains the progress of upload.
     * `-1` for indeterminate.
     */
    val isCreating = MutableLiveData<CreationStep?>(null)

    /**
     * Stores whether a process of deletion is being performed. This is used by the deletion
     * dialog to block the user interaction until the operation is complete.
     */
    val isDeleting = MutableLiveData<Boolean>()

    /**
     * Whether the form is completely filled or not. The creation button should be disabled if
     * this is not true.
     */
    abstract val isFilled: StateFlow<Boolean>

    abstract val hasParent: Boolean

    val parent = MutableLiveData<ParentType>()

    val element = MutableLiveData<ElementType?>()

    /**
     * If the server returns an error while performing an operation, it should be passed here,
     * and the UI will be updated.
     */
    val serverError = MutableLiveData<RequestException?>()

    /**
     * For appending all the data required for creating the object in the server. Will be sent
     * to the creation endpoint. Automatically appends the data in [image] when it's not null.
     */
    protected abstract fun FormBuilder.getFormData()

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
                this@EditorModel.parent.postValue(parent)

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
                    element.postValue(child)

                    (child as? ImageEntity?)?.let { imageUUID.postValue(it.imageUUID) }

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
        image.value?.recycle()
        Timber.d("Cleared View Model.")
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun create() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            isCreating.postValue(CreationStep.Compressing)

            val originalUUID = (element.value as? ImageEntity?)?.imageUUID
            val currentImageUUID = imageUUID.value

            val imageBytes = if (
            // if creating new area
                originalUUID == null ||
                // or image updated
                currentImageUUID == null ||
                originalUUID != currentImageUUID
            )
                image.value?.let { image ->
                    Timber.d("Compressing image...")
                    ByteArrayOutputStream()
                        .apply {
                            image.compress(BitmapCompat.WEBP_LOSSLESS, 100, this)
                        }
                        .toByteArray()
                }
            else
                null

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
                    getFormData()
                }
            ) {
                val apiKey = Preferences.getApiKey(getApplication()).firstOrNull()
                header(HttpHeaders.Authorization, "Bearer $apiKey")

                onUpload { bytesSentTotal, contentLength ->
                    isCreating.postValue(
                        CreationStep.Uploading((bytesSentTotal.toDouble() / contentLength).toFloat())
                    )
                }
            }
            try {
                val status = response.status
                val body = response.bodyAsJson()

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
                isCreating.postValue(CreationStep.Finishing)

                val data = body.getJSONObject("data")
                val elementJson = data.getJSONObject("element")
                val element = elementSerializer.fromJson(elementJson)

                if (element is ImageEntity) {
                    element.updateImageIfNeeded(getApplication())
                }

                operation(element)
            } catch (e: RequestException) {
                withContext(Dispatchers.Main) { serverError.value = e }
            } catch (e: Exception) {
                Timber.e(e, "Could not create or update.")
                throw e
            }

            isCreating.postValue(null)
        }
    }

    /**
     * Starts deleting [element] and all of its children. Updates [isDeleting] to `true` at
     * start, and to `false` at end.
     */
    fun delete() = viewModelScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            try {
                isDeleting.postValue(true)

                val element = element.value
                if (element == null) {
                    Timber.w("Won't delete anything since element is null.")
                } else {
                    Timber.i("Deleting $element recursively.")
                    dao.deleteRecursively(element)
                }
            } finally {
                isDeleting.postValue(false)
            }
        }
    }

    fun loadImage(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        isLoadingImage.postValue(true)
        getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
            image.postValue(bitmap)
            imageUUID.postValue(null)
        }
        isLoadingImage.postValue(false)
    }

    private fun loadFile(
        uri: Uri,
        nameState: MutableLiveData<String?>,
        onDataLoaded: (ByteArray) -> Unit
    ) = viewModelScope.launch(Dispatchers.IO) {
        val fileName = getApplication<Application>().getFileName(uri)
        if (fileName == null) Timber.w("Could not get name for file at $uri.")
        withContext(Dispatchers.Main) { nameState.value = fileName }

        getApplication<Application>().contentResolver.openFileDescriptor(uri, "r")?.use { pdf ->
            FileInputStream(pdf.fileDescriptor).use { stream ->
                val bytes = stream.readBytes()
                onDataLoaded(bytes)
            }
        }
    }

    fun loadKmz(uri: Uri) = loadFile(uri, kmzName) {
        Timber.d("Picked new KMZ file: ${kmzName.value}. Size: ${it.size}")
        kmzData = it
    }

    fun loadGpx(uri: Uri) = loadFile(uri, gpxName) {
        Timber.d("Picked new GPX file: ${gpxName.value}. Size: ${it.size}")
        gpxData = it
    }

    open class CreationStep(@StringRes val messageRes: Int) {
        object Compressing : CreationStep(R.string.creation_step_compressing)
        class Uploading(progress: Float) :
            ProgressStep(R.string.creation_step_uploading, progress)

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
}
