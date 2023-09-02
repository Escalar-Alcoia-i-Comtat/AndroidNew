package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.ChevronLeft
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.UriUtils.getFileName
import org.escalaralcoiaicomtat.android.utils.compat.BitmapCompat
import org.escalaralcoiaicomtat.android.utils.letIf
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.escalaralcoiaicomtat.android.utils.toMap
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class EditorActivity<
    ParentType : BaseEntity?,
    ElementType : BaseEntity,
    Model : EditorActivity.EditorModel<ParentType, ElementType>
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

        const val EXTRA_PARENT_ID: String = "parentId"
        const val EXTRA_PARENT_NAME: String = "parentName"
        const val EXTRA_ELEMENT_ID: String = "elementId"

        const val RESULT_EXCEPTION: String = "exception"
    }

    data class Input(
        val parentName: String?,
        val parentId: Long?,
        val elementId: Long?
    ) {
        constructor(
            parentName: String,
            parentId: Long
        ) : this(parentName, parentId, null)

        companion object {
            fun fromParent(parent: DataEntity) = Input(parent.displayName, parent.id)

            fun fromElement(parent: DataEntity, element: BaseEntity) = Input(
                parent.displayName,
                parent.id,
                element.id
            )
        }
    }

    sealed class Result {
        data object CreateSuccess: Result()

        data object EditSuccess: Result()

        data class Failure(val throwable: Throwable?): Result()

        data object CreateCancelled: Result()

        data object EditCancelled: Result()
    }

    abstract class ResultContract<A : EditorActivity<*, *, *>>(
        private val kClass: KClass<A>
    ) : ActivityResultContract<Input?, Result>() {
        override fun createIntent(context: Context, input: Input?): Intent =
            Intent(context, kClass.java).apply {
                putExtra(EXTRA_PARENT_ID, input?.parentId)
                putExtra(EXTRA_PARENT_NAME, input?.parentName)
                putExtra(EXTRA_ELEMENT_ID, input?.elementId)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Result =
            when (resultCode) {
                RESULT_CREATE_OK -> Result.CreateSuccess
                RESULT_EDIT_OK -> Result.EditSuccess
                RESULT_CREATE_CANCELLED -> Result.CreateCancelled
                RESULT_EDIT_CANCELLED -> Result.EditCancelled
                else -> {
                    val throwable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent?.getSerializableExtra(RESULT_EXCEPTION, Throwable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent?.getSerializableExtra(RESULT_EXCEPTION) as Throwable
                    }
                    Result.Failure(throwable)
                }
            }
    }

    protected abstract val model: Model

    protected val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            CoroutineScope(Dispatchers.IO).launch {
                model.isLoadingImage.postValue(true)
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                    model.image.postValue(bitmap)
                }
                model.isLoadingImage.postValue(false)
            }
        }

    protected val kmzPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            CoroutineScope(Dispatchers.IO).launch {
                model.kmzName.postValue(getFileName(uri))

                contentResolver.openFileDescriptor(uri, "r")?.use { pdf ->
                    FileInputStream(pdf.fileDescriptor).use { stream ->
                        val bytes = stream.readBytes()
                        model.kmzData = bytes
                    }
                }
            }
        }

    protected open val isScrollable: Boolean = true

    protected open val maxWidth: Int = 1000

    protected val parentId: Long? by extras()
    protected val parentName: String? by extras()

    protected val elementId: Long? by extras()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extrasString = intent.extras?.toMap()?.toList()?.joinToString { (k, v) -> "$k=$v" }
        Timber.i("Launched ${this::class.simpleName} with extras: $extrasString")

        setContentThemed {
            BackInvokeHandler(onBack = ::onBack)

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            val element by model.element.observeAsState()

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
                            val parent by model.parent.observeAsState()

                            if (!model.hasParent || parent != null) {
                                SidePanel(parent)

                                Column(
                                    modifier = Modifier
                                        .widthIn(max = maxWidth.dp)
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .letIf(isScrollable) { it.verticalScroll(rememberScrollState()) },
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
                                        progress = step.progress
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
     * Should be overridden with the contents of the editor such as text inputs. Scrollable if
     * [isScrollable].
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
    protected open fun RowScope.SidePanel(parent: ParentType?) {}

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
                        if (throwable == null) {
                            Timber.i("Creation successful")

                            if (model.element.value == null)
                                setResult(RESULT_CREATE_OK)
                            else
                                setResult(RESULT_EDIT_OK)
                        } else {
                            Timber.e(throwable, "Creation failed.")

                            setResult(
                                RESULT_FAILED,
                                Intent().apply { putExtra(RESULT_EXCEPTION, throwable) }
                            )
                        }
                        finish()
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
        M : EditorModel<ParentType, ElementType>,
        Z : EditorActivity<ParentType, ElementType, M>,
        T : Any
        > extras(): ReadOnlyProperty<Z, T?> = ReadOnlyProperty { _, property ->
        intent?.extras?.get(property.name) as? T?
    }

    abstract class EditorModel<ParentType : BaseEntity?, ElementType : BaseEntity>(
        application: Application,
        protected val parentId: Long?,
        protected val elementId: Long?
    ) : AndroidViewModel(application) {
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
         * Used together with [kmzData] to hold the currently selected KMZ file if any. This
         * variable stores the name of the KMZ file selected.
         */
        val kmzName = MutableLiveData<String?>(null)

        /**
         * Used together with [kmzName] to hold the currently selected KMZ file if any. This
         * variable holds the contents of the KMZ file selected.
         */
        var kmzData: ByteArray? = null

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
         * Whether the form is completely filled or not. The creation button should be disabled if
         * this is not true.
         */
        abstract val isFilled: MediatorLiveData<Boolean>

        abstract val hasParent: Boolean

        val parent = MutableLiveData<ParentType>()

        val element = MutableLiveData<ElementType>()

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
        protected abstract suspend fun insertDao(element: ElementType)

        /**
         * Should use [dao] to update the given [element].
         */
        protected abstract suspend fun updateDao(element: ElementType)

        init {
            viewModelScope.launch(Dispatchers.IO) {
                if (!hasParent) {
                    // If fetch parent is null, type doesn't have parent
                } else {
                    val parent = fetchParent(parentId!!)
                    if (parent == null) {
                        Timber.e("Could not get a valid parent with id $parentId")
                        whenNotFound?.invoke()
                        return@launch
                    }
                    this@EditorModel.parent.postValue(parent)

                    val parentName = parent.let { it::class.simpleName }
                    Timber.d("Initializing data for $parentName #$parentId")
                    init(parent)
                }

                val child = elementId?.let { fetchChild(it) }
                if (child != null) {
                    element.postValue(child)

                    fill(child)
                }
            }
        }

        override fun onCleared() {
            image.value?.recycle()
            Timber.d("Cleared View Model.")
        }

        fun create() = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                isCreating.postValue(CreationStep.Compressing)

                val imageBytes = image.value?.let { image ->
                    Timber.d("Compressing image...")
                    ByteArrayOutputStream()
                        .apply {
                            image.compress(BitmapCompat.WEBP_LOSSLESS, 100, this)
                        }
                        .toByteArray()
                }

                val extraData = prepareData()

                Timber.d("Sending data to server...")
                ktorHttpClient.submitFormWithBinaryData(
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
                }.apply {
                    when (status) {
                        HttpStatusCode.Created -> {
                            Timber.d("Creation complete (status=$status). Synchronizing...")
                            isCreating.postValue(CreationStep.Finishing)

                            val data = JSONObject(bodyAsText()).getJSONObject("data")
                            val elementJson = data.getJSONObject("element")
                            val element = elementSerializer.fromJson(elementJson)
                            insertDao(element)
                        }
                        HttpStatusCode.OK -> {
                            Timber.d("Update complete (status=$status). Synchronizing...")
                            isCreating.postValue(CreationStep.Finishing)

                            val data = JSONObject(bodyAsText()).getJSONObject("data")
                            val elementJson = data.getJSONObject("element")
                            val element = elementSerializer.fromJson(elementJson)
                            updateDao(element)
                        }
                        else -> {
                            throw RequestException(status, bodyAsJson())
                        }
                    }
                }

                Timber.d("Synchronization complete.")
                isCreating.postValue(null)
            }
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
