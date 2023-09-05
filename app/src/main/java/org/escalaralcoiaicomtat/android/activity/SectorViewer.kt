package org.escalaralcoiaicomtat.android.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewPathActivity
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.bodyAsJson
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.storage.type.color
import org.escalaralcoiaicomtat.android.ui.dialog.AddBlockDialog
import org.escalaralcoiaicomtat.android.ui.list.PathItem
import org.escalaralcoiaicomtat.android.ui.reusable.CardWithIconAndTitle
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import org.escalaralcoiaicomtat.android.ui.reusable.DropdownChip
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalFoundationApi::class
)
class SectorViewer : AppCompatActivity() {
    companion object {
        const val EXTRA_SECTOR_ID = "sector_id"
    }

    data class Input(
        val sectorId: Long
    )

    object Contract : ActivityResultContract<Input, Void?>() {
        override fun createIntent(context: Context, input: Input): Intent =
            Intent(context, SectorViewer::class.java).apply {
                putExtra(EXTRA_SECTOR_ID, input.sectorId)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    }

    private val viewModel: Model by viewModels()

    private val newPathLauncher = registerForActivityResult(NewPathActivity.Contract) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val sectorId = extras?.getLong(EXTRA_SECTOR_ID, -1)
        if (sectorId == null || sectorId < 0) {
            Timber.e("Sector ID not specified, going back...")
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        viewModel.loadSector(this, sectorId)

        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.selectionIndex.value != null) {
                viewModel.selectionIndex.postValue(null)
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        setContentThemed {
            val sector by viewModel.sector.observeAsState()
            val paths by viewModel.paths.observeAsState()

            val filters = remember { mutableStateListOf<Filter>() }

            var showingFiltersModal by remember { mutableStateOf(false) }
            if (showingFiltersModal) {
                ModalBottomSheet(
                    onDismissRequest = { showingFiltersModal = false },
                    windowInsets = WindowInsets.safeDrawing
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        val gradeFilter = filters.filterIsInstance<Filter.Grade>().firstOrNull()

                        Text(
                            text = stringResource(R.string.filter_grade_title),
                            style = MaterialTheme.typography.labelLarge
                        )
                        DropdownChip(
                            label = stringResource(R.string.filter_grade_from),
                            options = SportsGrade.entries.map {
                                DropdownChip.Option(
                                    text = { it.displayName },
                                    highlighted = gradeFilter?.grades?.contains(it) ?: false
                                )
                            },
                            active = gradeFilter != null,
                            onSelected = { index ->
                                val newList = (gradeFilter?.grades ?: emptyList()).toMutableList()
                                newList.add(SportsGrade.entries[index])

                                filters.removeIf { it is Filter.Grade }
                                filters.add(
                                    Filter.Grade(newList)
                                )
                            }
                        )
                    }
                }
            }

            Scaffold(
                topBar = {
                    val apiKey by Preferences.getApiKey(this).collectAsState(initial = null)

                    AnimatedContent(
                        targetState = sector,
                        label = "action-bar-visibility"
                    ) { sector ->
                        if (sector != null) {
                            TopAppBar(
                                title = { Text(sector.displayName) },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            setResult(Activity.RESULT_CANCELED)
                                            finish()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Rounded.ChevronLeft,
                                            stringResource(R.string.action_back)
                                        )
                                    }
                                },
                                actions = {
                                    if (apiKey != null) {
                                        IconButton(
                                            onClick = {
                                                newPathLauncher.launch(
                                                    EditorActivity.Input.fromParent(sector)
                                                )
                                            }
                                        ) {
                                            Icon(
                                                Icons.Rounded.Add,
                                                stringResource(R.string.action_create)
                                            )
                                        }
                                    }

                                    /* todo - filter
                                    IconButton(
                                        onClick = { showingFiltersModal = true }
                                    ) {
                                        Icon(
                                            Icons.Rounded.FilterList,
                                            stringResource(R.string.action_filter),
                                            tint = if (filters.isEmpty()) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    }*/
                                }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                AnimatedContent(
                    targetState = sector to paths,
                    label = "animate-sector-loading"
                ) { (sector, paths) ->
                    if (sector == null || paths == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            Content(sector, paths)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RowScope.Content(sector: Sector, paths: List<Path>) {
        val context = LocalContext.current
        val windowSizeClass = calculateWindowSizeClass(this@SectorViewer)

        val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

        val imageFile by sector.rememberImageFile().observeAsState()
        var progress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

        val blocks by viewModel.blocks.observeAsState(initial = emptyMap())

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                if (imageFile != null) return@withContext

                sector.updateImageIfNeeded(context, null) { current, max ->
                    progress = current.toInt() to max.toInt()
                }
            }
        }

        if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val selectedPath by viewModel.selectionIndex.observeAsState()

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(paths, key = { _, path -> path.id }) { index, path ->
                        PathItem(path, blocks = blocks[path] ?: emptyList(), apiKey = apiKey) {
                            viewModel.selectionIndex.postValue(index)
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedPath,
                    label = "animate-bottom-path-info",
                    transitionSpec = {
                        if (targetState == null || initialState == null) {
                            // moving from open to closed, or from closed to open
                            slideInVertically { -it } togetherWith slideOutVertically { it }
                        } else if (initialState != null && targetState != null) {
                            // moving from one path to another
                            if (initialState!! < targetState!!) {
                                // moving from left to right
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            } else {
                                // moving from right to left
                                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    }
                ) { selectedIndex ->
                    val path = selectedIndex?.let(paths::get)
                    if (path != null) {
                        PathInformation(
                            path,
                            apiKey = apiKey,
                            blocks = blocks[path] ?: emptyList(),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            onNextRequested = if (selectedIndex + 1 >= paths.size)
                                null
                            else {
                                { viewModel.selectionIndex.postValue(selectedIndex + 1) }
                            },
                            onPreviousRequested = if (selectedIndex <= 0)
                                null
                            else {
                                { viewModel.selectionIndex.postValue(selectedIndex - 1) }
                            },
                            onEditRequested = {
                                newPathLauncher.launch(
                                    EditorActivity.Input.fromElement(sector, path)
                                )
                            }
                        ) { viewModel.selectionIndex.postValue(null) }
                    }
                }
            }
        }

        imageFile?.let { image ->
            val zoomState = rememberZoomState()

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clipToBounds()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image)
                            .crossfade(true)
                            .build(),
                        contentDescription = sector.displayName,
                        modifier = Modifier
                            .zoomable(zoomState, enableOneFingerZoom = false)
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                    BottomPathsView(sector, paths)
                }
            }
        } ?: Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded)
                        1f
                    else
                        .7f
                )
        ) {
            CircularProgressIndicator(progress)
        }
    }

    @Composable
    fun BottomPathsView(sector: Sector, paths: List<Path>) {
        val apiKey by Preferences.getApiKey(this).collectAsState(initial = null)

        val selectionIndex by viewModel.selectionIndex.observeAsState()

        val blocks by viewModel.blocks.observeAsState(initial = emptyMap())

        AnimatedContent(
            targetState = selectionIndex,
            label = "paths-list"
        ) { selectedIndex ->
            val path = selectedIndex?.let(paths::get)
            if (path == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(.35f)
                ) {
                    itemsIndexed(
                        items = paths,
                        key = { _, path -> path.id }
                    ) { index, path ->
                        PathItem(path, blocks = blocks[path] ?: emptyList(), apiKey = apiKey) {
                            viewModel.selectionIndex.postValue(index)
                        }
                    }
                }
            } else {
                PathInformation(
                    path,
                    blocks = blocks[path] ?: emptyList(),
                    apiKey = apiKey,
                    modifier = Modifier.fillMaxHeight(.5f),
                    onNextRequested = if (selectedIndex >= paths.size)
                        null
                    else {
                        { viewModel.selectionIndex.postValue(selectedIndex + 1) }
                    },
                    onPreviousRequested = if (selectedIndex <= 0)
                        null
                    else {
                        { viewModel.selectionIndex.postValue(selectedIndex - 1) }
                    },
                    onEditRequested = {
                        newPathLauncher.launch(
                            EditorActivity.Input.fromElement(sector, path)
                        )
                    }
                ) { viewModel.selectionIndex.postValue(null) }
            }
        }
    }

    @Composable
    fun PathInformation(
        path: Path,
        blocks: List<Blocking>,
        apiKey: String?,
        modifier: Modifier = Modifier,
        onPreviousRequested: (() -> Unit)?,
        onNextRequested: (() -> Unit)?,
        onEditRequested: () -> Unit,
        onDismissRequested: () -> Unit
    ) {
        var showingCreateBlockDialog by remember { mutableStateOf(false) }
        var editingBlock: Blocking? by remember { mutableStateOf(null) }
        if (showingCreateBlockDialog || editingBlock != null) {
            AddBlockDialog(
                pathId = path.id,
                blocking = editingBlock,
                onCreationRequest = { blocking ->
                    if (blocking.id == 0L) {
                        viewModel.createBlock(blocking)
                    } else {
                        viewModel.updateBlock(blocking)
                    }

                    editingBlock = null
                    showingCreateBlockDialog = false
                },
                onDeleteRequest = {
                    viewModel.deleteBlock(editingBlock!!)

                    editingBlock = null
                    showingCreateBlockDialog = false
                },
                onDismissRequest = {
                    showingCreateBlockDialog = false
                    editingBlock = null
                }
            )
        }

        OutlinedCard(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onPreviousRequested?.let { function ->
                    IconButton(onClick = function) {
                        Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.action_previous))
                    }
                }
                onNextRequested?.let { function ->
                    IconButton(onClick = function) {
                        Icon(Icons.Rounded.ChevronRight, stringResource(R.string.action_next))
                    }
                }
                Text(
                    text = path.displayName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                if (apiKey != null) {
                    IconButton(onClick = { showingCreateBlockDialog = true }) {
                        Icon(Icons.Outlined.AddAlert, stringResource(R.string.action_add))
                    }
                    IconButton(onClick = onEditRequested) {
                        Icon(Icons.Rounded.Edit, stringResource(R.string.action_edit))
                    }
                }
                IconButton(onClick = onDismissRequested) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.action_close))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                blocks.forEach { block ->
                    val type = block.type
                    val shouldDisplay = block.shouldDisplay()

                    if (!shouldDisplay && apiKey == null) {
                        // if not should display, and not authorized, hide block
                        return@forEach
                    }

                    CardWithIconAndTitle(
                        iconRes = type.iconRes,
                        title = stringResource(type.titleRes),
                        message = stringResource(type.messageRes),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        border = if (shouldDisplay)
                            CardDefaults.outlinedCardBorder()
                        else
                            BorderStroke(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            ),
                        onClick = {
                            editingBlock = block
                        }.takeIf { apiKey != null }
                    ) {
                        block.recurrence?.let { recurrence ->
                            Text(
                                text = stringResource(
                                    R.string.block_recurrence,
                                    recurrence.fromDay.toString() + " " +
                                        recurrence.fromMonth.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        ),
                                    recurrence.toDay.toString() + " " +
                                        recurrence.toMonth.getDisplayName(
                                            TextStyle.FULL,
                                            Locale.getDefault()
                                        )
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                fontStyle = if (shouldDisplay)
                                    FontStyle.Normal
                                else
                                    FontStyle.Italic
                            )
                        }
                        block.endDate?.let { endDate ->
                            Text(
                                text = stringResource(
                                    R.string.block_end_date,
                                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                fontStyle = if (shouldDisplay)
                                    FontStyle.Normal
                                else
                                    FontStyle.Italic
                            )
                        }
                    }
                }
                path.grade?.let { grade ->
                    CardWithIconAndTitle(
                        iconRes = R.drawable.climbing_shoes,
                        title = stringResource(R.string.path_view_grade_title),
                        message = stringResource(R.string.path_view_grade_message),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = grade.displayName,
                            color = grade.color.current,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                path.ropeLength?.let { ropeLength ->
                    CardWithIconAndTitle(
                        iconRes = R.drawable.rope,
                        title = stringResource(R.string.path_view_height_title),
                        message = stringResource(
                            R.string.path_view_height_message,
                            path.height!!,
                            ropeLength
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                path.stringCount?.takeIf { it > 0 }?.let { stringCount ->
                    CardWithIconAndTitle(
                        iconRes = R.drawable.quickdraw,
                        title = stringResource(R.string.path_view_quickdraw_count_title),
                        message = stringResource(
                            R.string.path_view_quickdraw_count_message,
                            stringCount
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (path.anyCount) {
                    val list = StringBuilder()
                    path.parabolts?.let { list.appendLine("- ${it.text}") }
                    path.burils?.let { list.appendLine("- ${it.text}") }
                    path.pitons?.let { list.appendLine("- ${it.text}") }
                    path.spits?.let { list.appendLine("- ${it.text}") }
                    path.tensors?.let { list.appendLine("- ${it.text}") }

                    CardWithIconAndTitle(
                        iconRes = R.drawable.climbing_anchor,
                        title = stringResource(R.string.path_view_count_title),
                        message = stringResource(
                            R.string.path_view_count_message,
                            list.toString()
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (path.anyRequired) {
                    val list = StringBuilder()
                    path.cracker?.let { list.appendLine("- ${it.text}") }
                    path.friend?.let { list.appendLine("- ${it.text}") }
                    path.lanyard?.let { list.appendLine("- ${it.text}") }
                    path.nail?.let { list.appendLine("- ${it.text}") }
                    path.piton?.let { list.appendLine("- ${it.text}") }
                    path.stapes?.let { list.appendLine("- ${it.text}") }

                    CardWithIconAndTitle(
                        iconRes = R.drawable.energy_absorber,
                        title = stringResource(R.string.path_view_required_title),
                        message = stringResource(
                            R.string.path_view_required_message,
                            list.toString()
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                path.description?.takeIf { path.showDescription }?.let { description ->
                    CardWithIconAndTitle(
                        iconRes = R.drawable.baseline_description,
                        title = stringResource(R.string.path_view_description),
                        message = description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    class Model(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val dao = database.dataDao()

        private val _sector = MutableLiveData<Sector>()
        val sector: LiveData<Sector?> get() = _sector

        private val _paths = MutableLiveData<List<Path>>()
        val paths: LiveData<List<Path>> get() = _paths

        private val _blocks = MutableLiveData<Map<Path, List<Blocking>>>()
        val blocks: LiveData<Map<Path, List<Blocking>>> get() = _blocks

        val selectionIndex: MutableLiveData<Int?> = MutableLiveData(null)

        fun loadSector(lifecycleOwner: LifecycleOwner, sectorId: Long) =
            viewModelScope.launch(Dispatchers.IO) {
                val sector = dao.getSector(sectorId)
                    ?: throw IllegalArgumentException("Could not find sector with id $sectorId")
                _sector.postValue(sector)

                val paths = dao.getPathWithBlocksLive(sector.id)
                withContext(Dispatchers.Main) {
                    paths.observe(lifecycleOwner) { pathsWithBlocks ->
                        val list = pathsWithBlocks.map { it.path }.sorted()

                        _paths.postValue(list)
                        _blocks.postValue(
                            pathsWithBlocks.associate { it.path to it.blocks }
                        )
                    }
                }
            }

        fun createBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            val apiKey = Preferences.getApiKey(getApplication()).first()

            ktorHttpClient.post(EndpointUtils.getUrl("block/${blocking.pathId}")) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(blocking.toJson().toString())
            }.apply {
                if (status == HttpStatusCode.Created) {
                    // Update successful
                    Timber.d("Created block successfully.")

                    val element = bodyAsJson().getJSONObject("element").let(Blocking::fromJson)
                    dao.insert(element)
                } else {
                    Timber.e("Could not create block in server.")
                    throw RequestException(status, bodyAsJson())
                }
            }
        }

        fun updateBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            dao.update(blocking)
        }

        fun deleteBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            dao.notifyDeletion(
                LocalDeletion(type = "block", deleteId = blocking.id)
            )
            dao.delete(blocking)
        }
    }

    sealed class Filter {
        data class Grade(
            val grades: List<GradeValue>
        ) : Filter()
    }
}
