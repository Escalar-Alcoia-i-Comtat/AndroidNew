package org.escalaralcoiaicomtat.android.activity.creation

import android.app.Activity
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
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
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
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
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.UriUtils.getFileName
import org.escalaralcoiaicomtat.android.utils.await
import org.escalaralcoiaicomtat.android.utils.compat.BitmapCompat
import org.escalaralcoiaicomtat.android.utils.letIf
import org.escalaralcoiaicomtat.android.utils.toMap
import org.escalaralcoiaicomtat.android.utils.toast
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
abstract class CreatorActivity<
    ParentType : BaseEntity?,
    ChildType : BaseEntity,
    Model : CreatorActivity.CreatorModel<ParentType, ChildType>
    >(
    @StringRes private val titleRes: Int
) : AppCompatActivity() {
    companion object {
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

            fun fromElement(parent: DataEntity, element: DataEntity) = Input(
                parent.displayName,
                parent.id,
                element.id
            )
        }
    }

    abstract class ResultContract<A : CreatorActivity<*, *, *>>(
        private val kClass: KClass<A>
    ) : ActivityResultContract<Input?, Throwable?>() {
        override fun createIntent(context: Context, input: Input?): Intent =
            Intent(context, kClass.java).apply {
                putExtra(EXTRA_PARENT_ID, input?.parentId)
                putExtra(EXTRA_PARENT_NAME, input?.parentName)
                putExtra(EXTRA_ELEMENT_ID, input?.elementId)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Throwable? =
            when (resultCode) {
                Activity.RESULT_OK -> null
                Activity.RESULT_CANCELED -> CancellationException("User pressed back.")
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getSerializableExtra(RESULT_EXCEPTION, Throwable::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent?.getSerializableExtra(RESULT_EXCEPTION) as Throwable
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
                        title = { Text(stringResource(titleRes)) },
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
                        Column(
                            modifier = Modifier
                                .widthIn(max = maxWidth.dp)
                                .fillMaxSize()
                                .letIf(isScrollable) { it.verticalScroll(rememberScrollState()) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Content()
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
                                is CreatorModel.ProgressStep ->
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

    @Composable
    protected abstract fun ColumnScope.Content()

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
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Timber.e("Could not create.", throwable)

                            toast(R.string.creation_error_toast)
                        }
                    }
                },
                enabled = isFilled && isCreating == null
            ) {
                Text(stringResource(R.string.action_create))
            }
        }
    }

    protected open fun onBack() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("Stopped ${this::class.simpleName}")
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private fun <
        ParentType : BaseEntity?,
        ChildType : BaseEntity,
        M : CreatorModel<ParentType, ChildType>,
        Z : CreatorActivity<ParentType, ChildType, M>,
        T : Any
        > extras(): ReadOnlyProperty<Z, T?> = ReadOnlyProperty { _, property ->
        intent?.extras?.get(property.name) as? T?
    }

    abstract class CreatorModel<ParentType : BaseEntity?, ChildType : BaseEntity>(
        application: Application,
        protected val parentId: Long?,
        private val childId: Long?
    ) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        protected val dao = database.dataDao()

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

        protected abstract val hasParent: Boolean

        val parent = MutableLiveData<ParentType>()

        val element = MutableLiveData<ChildType>()

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
        open suspend fun fill(child: ChildType) {}

        open suspend fun fetchParent(parentId: Long): ParentType? = null

        open suspend fun fetchChild(childId: Long): ChildType? = null

        /**
         * Can be used for preparing data to send in the form asynchronously. For example,
         * compressing files, or doing heavy calculations.
         */
        open suspend fun prepareData(): FormBuilder.() -> Unit = {}

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
                    this@CreatorModel.parent.postValue(parent)

                    init(parent)

                    val child = childId?.let { fetchChild(it) }
                    if (child != null) {
                        fill(child)
                    }
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
                    EndpointUtils.getUrl(creatorEndpoint),
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
                    if (status == HttpStatusCode.Created) {
                        Timber.d("Creation complete (status=$status). Synchronizing...")
                        isCreating.postValue(CreationStep.Finishing)

                        SyncWorker.synchronize(getApplication())
                            .await { it.state.isFinished }
                    } else {
                        throw RequestException(status, bodyAsJson())
                    }
                }

                Timber.d("Synchronization complete.")
                isCreating.postValue(null)
            }
        }

        open class CreationStep(@StringRes val messageRes: Int) {
            object Compressing : CreationStep(R.string.creation_step_compressing)
            class Uploading(progress: Float) :
                ProgressStep(R.string.creation_step_compressing, progress)

            object Finishing : CreationStep(R.string.creation_step_compressing)

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
