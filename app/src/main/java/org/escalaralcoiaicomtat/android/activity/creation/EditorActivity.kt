package org.escalaralcoiaicomtat.android.activity.creation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.BaseEntity
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.ui.form.FormField
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.toMap
import org.escalaralcoiaicomtat.android.utils.toast
import org.escalaralcoiaicomtat.android.viewmodel.editor.EditorModel
import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
abstract class EditorActivity<
    ParentType : BaseEntity?,
    ElementType : BaseEntity,
    ChildrenType : BaseEntity?,
    Model : EditorModel<ParentType, ElementType, ChildrenType>
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

            val isFilled by model.isFilled.collectAsState()
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
}
