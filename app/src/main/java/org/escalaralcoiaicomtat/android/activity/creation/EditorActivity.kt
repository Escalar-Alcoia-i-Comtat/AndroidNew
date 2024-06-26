package org.escalaralcoiaicomtat.android.activity.creation

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MediatorLiveData
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
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.util.UUID
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.bodyAsJson
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.dao.deleteRecursively
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.UriUtils.getFileName
import org.escalaralcoiaicomtat.android.utils.compat.BitmapCompat
import org.escalaralcoiaicomtat.android.utils.letIf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.escalaralcoiaicomtat.android.utils.toMap
import org.escalaralcoiaicomtat.android.utils.toast
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
abstract class EditorActivity<
    ParentType : BaseEntity?,
    ElementType : BaseEntity,
    ChildrenType : BaseEntity?,
    Model : EditorActivity.EditorModel<ParentType, ElementType, ChildrenType>
    >(
    @StringRes private val createTitleRes: Int,
    @StringRes private val editTitleRes: Int
) : AppCompatActivity() {
    companion object {
        const val RESULT_FAILED = 2
        const val RESULT_CREATE_CANCELLED = 3
        const val RESULT_EDIT_CANCELLED = 4
        const val RESULT_CREATE_OK = 5
        const val RESULT_EDIT_OK = 6
        const val RESULT_DELETE_OK = 7

        const val EXTRA_PARENT_ID: String = "parentId"
        const val EXTRA_ELEMENT_ID: String = "elementId"

        const val RESULT_EXCEPTION: String = "exception"
    }

    data class Input(
        val parentId: Long?,
        val elementId: Long?
    ) {
        constructor(
            parentId: Long
        ) : this(parentId, null)

        companion object {
            fun fromParent(parentId: Long) = Input(parentId)

            fun fromParent(parent: BaseEntity) = Input(parent.id)

            fun fromElement(parentId: Long, element: BaseEntity) = Input(
                parentId,
                element.id
            )

            fun fromElement(parent: BaseEntity, element: BaseEntity) = Input(
                parent.id,
                element.id
            )

            fun fromElement(element: BaseEntity) = Input(
                element.parentId,
                element.id
            )
        }
    }

    sealed class Result {
        data object CreateSuccess : Result()

        data object EditSuccess : Result()

        data class Failure(val throwable: Throwable?) : Result()

        data object CreateCancelled : Result()

        data object EditCancelled : Result()

        data object Deleted : Result()
    }

    abstract class ResultContract<A : EditorActivity<*, *, *, *>>(
        private val kClass: KClass<A>
    ) : ActivityResultContract<Input?, Result>() {
        override fun createIntent(context: Context, input: Input?): Intent =
            Intent(context, kClass.java).apply {
                putExtra(EXTRA_PARENT_ID, input?.parentId)
                putExtra(EXTRA_ELEMENT_ID, input?.elementId)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Result =
            when (resultCode) {
                RESULT_CREATE_OK -> Result.CreateSuccess
                RESULT_EDIT_OK -> Result.EditSuccess
                RESULT_CREATE_CANCELLED -> Result.CreateCancelled
                RESULT_EDIT_CANCELLED -> Result.EditCancelled
                RESULT_DELETE_OK -> Result.Deleted
                else -> {
                    val throwable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent?.getSerializableExtra(RESULT_EXCEPTION, Throwable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent?.getSerializableExtra(RESULT_EXCEPTION) as Throwable?
                    }
                    Result.Failure(throwable)
                }
            }
    }

    protected abstract val model: Model

    protected val imagePicker = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult
        model.loadImage(uri)
    }

    protected val kmzPicker = registerForActivityResult(OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        model.loadKmz(uri)
    }

    protected val gpxPicker = registerForActivityResult(OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        model.loadGpx(uri)
    }

    protected open val maxWidth: Int = 1000

    protected val parentId: Long? by extras()
    protected val parentName: String? by extras()

    protected val elementId: Long? by extras()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extrasString = intent.extras?.toMap()?.toList()?.joinToString { (k, v) -> "$k=$v" }
        Timber.i("Launched ${this::class.simpleName} with extras: $extrasString")

        setContentThemed {
            val windowSizeClass = calculateWindowSizeClass(this)

            PredictiveBackHandler { progress: Flow<BackEventCompat> ->
                // code for gesture back started
                try {
                    progress.collect { }
                    // code for completion
                    onBack()
                } catch (e: CancellationException) {
                    // code for cancellation
                }
            }

            val isDeleting by model.isDeleting.observeAsState(false)
            var requestedDeletion by remember { mutableStateOf(false) }
            if (requestedDeletion) {
                val element by model.element.observeAsState()

                AlertDialog(
                    onDismissRequest = { if (!isDeleting) requestedDeletion = false },
                    title = { Text(stringResource(R.string.delete_confirmation_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.delete_confirmation_message,
                                (element as? DataEntity)?.displayName
                                    ?: stringResource(R.string.none)
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                model.delete().invokeOnCompletion {
                                    if (it == null) {
                                        toast(R.string.delete_complete_toast)

                                        setResult(RESULT_DELETE_OK)
                                        finish()
                                    } else {
                                        toast(R.string.delete_error_toast)

                                        Timber.e(it, "Could not delete element")
                                    }
                                }
                            },
                            enabled = (element as? DataEntity) != null
                        ) {
                            Text(stringResource(R.string.action_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { requestedDeletion = false },
                            enabled = !isDeleting
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }

            val serverError by model.serverError.observeAsState()
            serverError?.let { error ->
                AlertDialog(
                    onDismissRequest = { model.serverError.postValue(null) },
                    title = { Text(stringResource(R.string.server_error_dialog_title)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(stringResource(R.string.server_error_dialog_headline, error.code))
                            error.message?.let {
                                Text(stringResource(R.string.server_error_dialog_message, it))
                            }
                            Text(stringResource(R.string.server_error_dialog_stacktrace))
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontFamily = FontFamily.Monospace,
                                            background = MaterialTheme.colorScheme.surfaceVariant,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        for (line in error.stackTrace) {
                                            appendLine(line.toString())
                                        }
                                    }
                                }
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { model.serverError.postValue(null) }) {
                            Text(stringResource(android.R.string.ok))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val clipboardManager =
                                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val data = ClipData.newPlainText(
                                    "Stacktrace",
                                    error.stackTraceToString()
                                )
                                clipboardManager.setPrimaryClip(data)
                            }
                        ) {
                            Text(stringResource(android.R.string.copy))
                        }
                    }
                )
            }

            Scaffold(
                topBar = {
                    val element by model.element.observeAsState()

                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    if (element == null) {
                                        createTitleRes
                                    } else {
                                        editTitleRes
                                    }
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = ::onBack) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronLeft,
                                    contentDescription = stringResource(R.string.action_back)
                                )
                            }
                        },
                        actions = {
                            if (element != null) {
                                IconButton(onClick = { requestedDeletion = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteForever,
                                        contentDescription = stringResource(R.string.action_delete)
                                    )
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    val parent by model.parent.observeAsState()

                    // Show side panel on top in mobile
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact && (!model.hasParent || parent != null)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(.3f)
                        ) {
                            SidePanel(parent)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .widthIn(max = maxWidth.dp)
                                .fillMaxSize()
                        ) {
                            if (!model.hasParent || parent != null) {
                                // Hide side panel on mobile
                                if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
                                    SidePanel(parent)
                                }

                                Column(
                                    modifier = Modifier
                                        .widthIn(max = maxWidth.dp)
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val editing by model.element.observeAsState()
                                    editing?.let {
                                        FormField(
                                            value = it.id.toString(),
                                            onValueChange = {},
                                            label = stringResource(R.string.form_id),
                                            readOnly = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Editor(parent)
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    val isCreating by model.isCreating.observeAsState()
                    AnimatedVisibility(
                        visible = isCreating != null,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        isCreating?.let { step ->
                            when (step) {
                                is EditorModel.ProgressStep ->
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        progress = { step.progress }
                                    )

                                else ->
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Footer()
                    }
                }
            }
        }
    }

    /**
     * Should be overridden with the contents of the editor such as text inputs.
     *
     * @param [parent] The parent element of the currently creating/editing one. Won't be `null` if
     * [EditorModel.hasParent] is true. Always `null` otherwise.
     */
    @Composable
    protected abstract fun ColumnScope.Editor(parent: ParentType?)

    /**
     * If any, can be overridden to show a panel on the left side of the editor. Use
     * [RowScope.weight] or width in contents to set size.
     */
    @Composable
    protected open fun RowScope.SidePanel(parent: ParentType?) {
    }

    @Composable
    private fun Footer() {
        Row(
            modifier = Modifier
                .widthIn(max = 1000.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current

            val element by model.element.observeAsState()

            val isFilled by model.isFilled.observeAsState(initial = false)
            val isCreating by model.isCreating.observeAsState()

            Text(
                text = isCreating?.getString(context) ?: "",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge
            )

            TextButton(
                onClick = {
                    model.create().invokeOnCompletion { throwable ->
                        val hasServerError = model.serverError.value != null
                        if (throwable == null && !hasServerError) {
                            Timber.i("Creation successful")

                            if (model.element.value == null)
                                setResult(RESULT_CREATE_OK)
                            else
                                setResult(RESULT_EDIT_OK)
                            finish()
                        } else if (!hasServerError) {
                            Timber.e(throwable, "Creation failed.")

                            setResult(
                                RESULT_FAILED,
                                Intent().apply { putExtra(RESULT_EXCEPTION, throwable) }
                            )
                            finish()
                        }
                    }
                },
                enabled = isFilled && isCreating == null
            ) {
                Text(
                    text = if (element == null)
                        stringResource(R.string.action_create)
                    else
                        stringResource(R.string.action_update)
                )
            }
        }
    }

    protected open fun onBack() {
        if (model.element.value == null)
            setResult(RESULT_CREATE_CANCELLED)
        else
            setResult(RESULT_EDIT_CANCELLED)
        finish()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("Stopped ${this::class.simpleName}")
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private fun <
        ParentType : BaseEntity?,
        ElementType : BaseEntity,
        ChildrenType : BaseEntity?,
        M : EditorModel<ParentType, ElementType, ChildrenType>,
        Z : EditorActivity<ParentType, ElementType, ChildrenType, M>,
        T : Any
        > extras(): ReadOnlyProperty<Z, T?> = ReadOnlyProperty { _, property ->
        intent?.extras?.get(property.name) as? T?
    }

    /**
     * Provides a ViewModel for interacting with the [EditorActivity]. All parent types must run
     * [onInit] when initialized:
     * ```kotlin
     * init {
     *   onInit()
     * }
     * ```
     */
    abstract class EditorModel<
        ParentType : BaseEntity?,
        ElementType : BaseEntity,
        ChildrenType : BaseEntity?
        >(
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
        abstract val isFilled: MediatorLiveData<Boolean>

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
}
