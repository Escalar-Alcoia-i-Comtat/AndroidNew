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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ChildFriendly
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewPathActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewSectorActivity
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.bodyAsJson
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.dao.toggleFavorite
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteSector
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.storage.relations.PathWithBlocks
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.storage.type.color
import org.escalaralcoiaicomtat.android.ui.dialog.AddBlockDialog
import org.escalaralcoiaicomtat.android.ui.icons.ClimbingAnchor
import org.escalaralcoiaicomtat.android.ui.icons.ClimbingShoes
import org.escalaralcoiaicomtat.android.ui.icons.EnergyAbsorber
import org.escalaralcoiaicomtat.android.ui.icons.Quickdraw
import org.escalaralcoiaicomtat.android.ui.icons.Rope
import org.escalaralcoiaicomtat.android.ui.icons.SlingHere
import org.escalaralcoiaicomtat.android.ui.list.PathItem
import org.escalaralcoiaicomtat.android.ui.reusable.CardWithIconAndTitle
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import org.escalaralcoiaicomtat.android.ui.reusable.DropdownChip
import org.escalaralcoiaicomtat.android.ui.reusable.InfoRow
import org.escalaralcoiaicomtat.android.ui.reusable.toolbar.ToolbarAction
import org.escalaralcoiaicomtat.android.ui.reusable.toolbar.ToolbarActionsOverflow
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.UriUtils.viewIntent
import org.escalaralcoiaicomtat.android.utils.canBeResolved
import timber.log.Timber

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

    private val viewModel: Model by viewModels {
        val extras = intent.extras
        val sectorId = extras?.getLong(EXTRA_SECTOR_ID, -1)
        if (sectorId == null || sectorId < 0) {
            Timber.e("Sector ID not specified, going back...")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        Model.Factory(sectorId ?: -1)
    }

    private val newSectorLauncher = registerForActivityResult(NewSectorActivity.Contract) {}

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

        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.selection.value != null) {
                viewModel.selection.postValue(null)
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        setContentThemed {
            val windowSizeClass = calculateWindowSizeClass(activity = this)

            val sector by viewModel.sector.observeAsState()
            val paths by viewModel.paths.observeAsState()

            val apiKey by viewModel.apiKey.observeAsState()
            val isFavorite by viewModel.isFavorite.observeAsState()

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
                                    ToolbarActionsOverflow(
                                        actions = listOfNotNull(
                                            // Sector Information
                                            ToolbarAction(
                                                { Icons.Outlined.Info },
                                                { stringResource(R.string.action_info) }
                                            ) {
                                                viewModel.selection.postValue(Model.Selection.SectorInformation)
                                            }.takeIf {
                                                windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded
                                            },
                                            // Favorite
                                            ToolbarAction(
                                                {
                                                    if (isFavorite != null)
                                                        Icons.Outlined.Bookmark
                                                    else
                                                        Icons.Outlined.BookmarkBorder
                                                },
                                                { stringResource(R.string.action_favorite) }
                                            ) {
                                                viewModel.toggleSectorFavorite()
                                            },
                                            // Edit
                                            ToolbarAction(
                                                { Icons.Rounded.Edit },
                                                { stringResource(R.string.action_edit) }
                                            ) {
                                                newSectorLauncher.launch(
                                                    EditorActivity.Input.fromElement(sector)
                                                )
                                            }.takeIf { apiKey != null },
                                            // Create
                                            ToolbarAction(
                                                { Icons.Rounded.Add },
                                                { stringResource(R.string.action_create) }
                                            ) {
                                                newPathLauncher.launch(
                                                    EditorActivity.Input.fromParent(sector)
                                                )
                                            }.takeIf { apiKey != null }
                                        ),
                                        maxItems = when (windowSizeClass.widthSizeClass) {
                                            WindowWidthSizeClass.Compact -> 2
                                            WindowWidthSizeClass.Medium -> 3
                                            WindowWidthSizeClass.Expanded -> 5
                                            // Fallback for edge cases
                                            else -> 2
                                        }
                                    )

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

        val imageFile by sector.rememberImageFile(false).observeAsState()
        var progress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

        val blocks by viewModel.blocks.observeAsState(initial = emptyMap())

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                if (imageFile != null) return@withContext

                sector.updateImageIfNeeded(context) { current, max ->
                    progress = current.toInt() to max.toInt()
                }
            }
        }

        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            SidePathsView(sector, paths, blocks, apiKey)
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

                if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
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

        val selection by viewModel.selection.observeAsState()

        val blocks by viewModel.blocks.observeAsState(initial = emptyMap())

        AnimatedContent(
            targetState = selection,
            label = "paths-list"
        ) { selected ->
            val selectedIndex = if (selected is Model.Selection.Index)
                selected.index
            else
                null
            val path = selectedIndex?.let(paths::get)
            val showingSectorInformation = selected is Model.Selection.SectorInformation

            if (showingSectorInformation) {
                SectorInformation(
                    sector = sector,
                    modifier = Modifier.fillMaxHeight(.5f)
                ) { viewModel.selection.postValue(null) }
            } else if (path == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(.35f)
                ) {
                    itemsIndexed(
                        items = paths,
                        key = { _, path -> path.id }
                    ) { index, path ->
                        PathItem(path, blocks = blocks[path] ?: emptyList(), apiKey = apiKey) {
                            viewModel.selection.postValue(Model.Selection.Index(index))
                        }
                    }
                }
            } else {
                PathInformation(
                    path = path,
                    blocks = blocks[path] ?: emptyList(),
                    apiKey = apiKey,
                    modifier = Modifier.fillMaxHeight(.5f),
                    onNextRequested = if (selectedIndex + 1 >= paths.size)
                        null
                    else {
                        { viewModel.selection.postValue(Model.Selection.Index(selectedIndex + 1)) }
                    },
                    onPreviousRequested = if (selectedIndex <= 0)
                        null
                    else {
                        { viewModel.selection.postValue(Model.Selection.Index(selectedIndex - 1)) }
                    },
                    onEditRequested = {
                        newPathLauncher.launch(
                            EditorActivity.Input.fromElement(sector, path)
                        )
                    }
                ) { viewModel.selection.postValue(null) }
            }
        }
    }

    @Composable
    fun RowScope.SidePathsView(
        sector: Sector,
        paths: List<Path>,
        blocks: Map<Path, List<Blocking>>,
        apiKey: String?
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            val selection by viewModel.selection.observeAsState()
            val selectedIndex = (selection as? Model.Selection.Index)?.index

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                populateSectorInformation(sector)

                stickyHeader(
                    key = "sector-children-title",
                    contentType = "title"
                ) {
                    Text(
                        text = stringResource(sector.childrenTitleRes),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(bottom = 4.dp)
                    )
                }
                itemsIndexed(paths, key = { _, path -> path.id }) { index, path ->
                    PathItem(path, blocks = blocks[path] ?: emptyList(), apiKey = apiKey) {
                        viewModel.selection.postValue(Model.Selection.Index(index))
                    }
                }
            }

            AnimatedContent(
                targetState = selectedIndex,
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
            ) { index ->
                val path = index?.let(paths::get)
                if (path != null) {
                    PathInformation(
                        path,
                        apiKey = apiKey,
                        blocks = blocks[path] ?: emptyList(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        onNextRequested = if (index + 1 >= paths.size)
                            null
                        else {
                            { viewModel.selection.postValue(Model.Selection.Index(index + 1)) }
                        },
                        onPreviousRequested = if (index <= 0)
                            null
                        else {
                            { viewModel.selection.postValue(Model.Selection.Index(index - 1)) }
                        },
                        onEditRequested = {
                            newPathLauncher.launch(
                                EditorActivity.Input.fromElement(sector, path)
                            )
                        }
                    ) { viewModel.selection.postValue(null) }
                }
            }
        }
    }

    private fun LazyListScope.populateSectorInformation(sector: Sector) {
        stickyHeader(
            key = "sector-information-title",
            contentType = "title"
        ) {
            Text(
                text = stringResource(R.string.list_sector_information_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 4.dp)
            )
        }
        if (sector.kidsApt) {
            item(
                key = "sector-children-apt",
                contentType = "sector-info"
            ) {
                InfoRow(
                    icon = Icons.Rounded.ChildFriendly,
                    iconContentDescription = stringResource(R.string.sector_kids_apt_title),
                    title = stringResource(R.string.sector_kids_apt_title),
                    subtitle = stringResource(R.string.sector_kids_apt_message)
                )
            }
        }
        item(
            key = "sector-sun-time",
            contentType = "sector-info"
        ) {
            val sunTime = sector.sunTime

            InfoRow(
                icon = sunTime.icon,
                iconContentDescription = stringResource(sunTime.label),
                title = stringResource(sunTime.title),
                subtitle = stringResource(sunTime.message)
            )
        }
        if (sector.point != null) {
            item(
                key = "sector-point",
                contentType = "sector-info"
            ) {
                val point = sector.point

                InfoRow(
                    icon = Icons.Rounded.Place,
                    iconContentDescription = stringResource(R.string.info_point_description),
                    title = stringResource(R.string.info_sector_location),
                    subtitle = "${point.latitude}, ${point.longitude}",
                    actions = listOfNotNull(
                        point
                            .uri(sector.displayName)
                            .viewIntent
                            .apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            .takeIf { it.canBeResolved(this@SectorViewer) }
                            ?.let { intent ->
                                Icons.Rounded.Map to {
                                    startActivity(intent)
                                }
                            }
                    )
                )
            }
        }
        if (sector.walkingTime != null) {
            item(
                key = "sector-walking-time",
                contentType = "sector-info"
            ) {
                val walkingTime = sector.walkingTime

                InfoRow(
                    icon = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    iconContentDescription = stringResource(R.string.sector_walking_time_title),
                    title = stringResource(R.string.sector_walking_time_title),
                    subtitle = pluralStringResource(
                        R.plurals.sector_walking_time_message,
                        walkingTime.toInt(),
                        walkingTime
                    )
                )
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
                    text = "${path.sketchId} - ",
                    modifier = Modifier
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = path.displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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
                        icon = type.icon,
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
                        icon = Icons.Filled.ClimbingShoes,
                        title = stringResource(R.string.path_view_grade_title),
                        message = stringResource(R.string.path_view_grade_message),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        trailingContent = {
                            Text(
                                text = grade.displayName,
                                color = grade.color.current,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )
                }
                path.heightUnits?.let { height ->
                    CardWithIconAndTitle(
                        icon = Icons.Filled.Rope,
                        title = stringResource(R.string.path_view_height_title),
                        message = stringResource(R.string.path_view_height_message),
                        trailingContent = {
                            Text(
                                text = height.decimalLabel(),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    )
                }
                path.pitches?.takeIf { it.isNotEmpty() }?.let { pitches ->
                    CardWithIconAndTitle(
                        icon = Icons.Filled.Landscape,
                        title = stringResource(R.string.path_view_pitches_title),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        extra = {
                            Spacer(Modifier.height(8.dp))

                            val density = LocalDensity.current

                            val labelTextStyle = MaterialTheme.typography.labelLarge
                            val textMeasurer = rememberTextMeasurer()
                            val pitchWidth = remember(pitches) {
                                with(density) {
                                    pitches.indices.maxOf {
                                        textMeasurer.measure(
                                            "L$it",
                                            labelTextStyle
                                        ).size.width
                                    }.toDp()
                                }
                            }
                            val gradesWidth = remember(pitches) {
                                with(density) {
                                    pitches.maxOf {
                                        if (it.gradeValue == null) 0
                                        else textMeasurer.measure(
                                            it.gradeValue.displayName,
                                            labelTextStyle
                                        ).size.width
                                    }.toDp()
                                }
                            }
                            val heightWidth = remember(pitches) {
                                with(density) {
                                    pitches.maxOf {
                                        if (it.height == null) 0
                                        else textMeasurer.measure(
                                            "${it.height}mm",
                                            labelTextStyle
                                        ).size.width
                                    }.toDp()
                                }
                            }

                            for ((i, pitch) in pitches.withIndex()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "L${i + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.width(pitchWidth)
                                    )

                                    Text(
                                        text = pitch.gradeValue?.displayName ?: "",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = pitch.gradeValue?.color?.current ?: Color.Black,
                                        modifier = Modifier.padding(start = 4.dp).width(gradesWidth)
                                    )

                                    Text(
                                        text = pitch.heightUnits?.decimalLabel() ?: "",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(start = 8.dp).width(heightWidth)
                                    )

                                    Text(
                                        text = pitch.ending?.let { "R${i + 1}" } ?: "",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    Text(
                                        text = pitch.ending?.let {
                                            stringResource(it.displayName)
                                        } ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                                    )
                                }
                                if (pitches.last() != pitch) HorizontalDivider()
                            }
                        }
                    )
                }
                path.stringCount?.takeIf { it > 0 }?.let { stringCount ->
                    CardWithIconAndTitle(
                        icon = Icons.Filled.Quickdraw,
                        title = stringResource(R.string.path_view_quickdraw_count_title),
                        message = pluralStringResource(
                            R.plurals.path_view_quickdraw_count_message,
                            stringCount.toInt(),
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
                        icon = Icons.Filled.ClimbingAnchor,
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
                        icon = Icons.Filled.EnergyAbsorber,
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
                        icon = Icons.Filled.Description,
                        title = stringResource(R.string.path_view_description),
                        message = description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (path.builder?.name != null || path.builder?.date != null || path.reBuilder?.isNotEmpty() == true) {
                    val text = StringBuilder()

                    val builder = path.builder
                    if (builder?.name != null && builder.date != null) {
                        text.appendLine(
                            stringResource(
                                R.string.path_view_builder_message_full,
                                builder.name,
                                builder.date
                            )
                        )
                    } else if (builder?.name != null) {
                        text.appendLine(
                            stringResource(R.string.path_view_builder_message_no_year, builder.name)
                        )
                    } else if (builder?.date != null) {
                        text.appendLine(
                            stringResource(R.string.path_view_builder_message_no_name, builder.date)
                        )
                    }

                    val reBuilderList = path.reBuilder?.takeIf { it.isNotEmpty() }
                    reBuilderList?.forEach { reBuilder ->
                        text.appendLine(
                            if (reBuilder.name != null && reBuilder.date != null) {
                                stringResource(
                                    R.string.path_view_builder_message_full,
                                    reBuilder.name,
                                    reBuilder.date
                                )
                            } else if (reBuilder.name != null) {
                                stringResource(
                                    R.string.path_view_builder_message_no_year,
                                    reBuilder.name
                                )
                            } else if (reBuilder.date != null) {
                                stringResource(
                                    R.string.path_view_builder_message_no_name,
                                    reBuilder.date
                                )
                            } else {
                                ""
                            }
                        )
                    }

                    CardWithIconAndTitle(
                        icon = Icons.Filled.Construction,
                        title = stringResource(R.string.path_view_builder_title),
                        message = text.toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                path.ending?.let { ending ->
                    CardWithIconAndTitle(
                        icon = Icons.Rounded.SlingHere,
                        title = stringResource(R.string.path_view_ending_title),
                        message = stringResource(
                            when (ending) {
                                Ending.WALKING -> R.string.path_view_ending_walking
                                Ending.RAPPEL -> R.string.path_view_ending_walking
                                else -> R.string.path_view_ending_message
                            }
                        ),
                        extra = {
                            Text(
                                text = stringResource(ending.displayName),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    /**
     * A dialog used in mobile-UI for displaying the Sector's information.
     */
    @Composable
    fun SectorInformation(sector: Sector, modifier: Modifier, onDismissRequested: () -> Unit) {
        OutlinedCard(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sector.displayName,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismissRequested) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.action_close))
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                populateSectorInformation(sector)
            }
        }
    }

    class Model(application: Application, private val sectorId: Long) :
        AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val dataDao = database.dataDao()
        private val userDao = database.userDao()

        private val _sector = MutableLiveData<Sector>()
        val sector: LiveData<Sector?> get() = _sector

        private var _isFavorite: LiveData<FavoriteSector?> = userDao.getSectorLive(sectorId)
        val isFavorite: LiveData<FavoriteSector?> get() = _isFavorite

        private val _paths = MutableLiveData<List<Path>>()
        val paths: LiveData<List<Path>> get() = _paths

        private val _blocks = MutableLiveData<Map<Path, List<Blocking>>>()
        val blocks: LiveData<Map<Path, List<Blocking>>> get() = _blocks

        val selection: MutableLiveData<Selection?> = MutableLiveData(null)

        val apiKey = Preferences.getApiKey(application).asLiveData(Dispatchers.Main)

        private lateinit var _pathWithBlocks: LiveData<List<PathWithBlocks>>
        private val pathWithBlocksObserver = Observer<List<PathWithBlocks>> { pathsWithBlocks ->
            val list = pathsWithBlocks.map { it.path }.sorted()

            _paths.postValue(list)
            _blocks.postValue(
                pathsWithBlocks.associate { it.path to it.blocks }
            )
        }

        init {
            if (sectorId >= 0) viewModelScope.launch(Dispatchers.IO) {
                val sector = dataDao.getSector(sectorId)
                    ?: throw IllegalArgumentException("Could not find sector with id $sectorId")
                _sector.postValue(sector)

                _pathWithBlocks = dataDao.getPathWithBlocksLive(sector.id)
                withContext(Dispatchers.Main) {
                    _pathWithBlocks.observeForever(pathWithBlocksObserver)
                }
            }
        }

        override fun onCleared() {
            _pathWithBlocks.removeObserver(pathWithBlocksObserver)
        }

        fun createBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            ktorHttpClient.post(EndpointUtils.getUrl("block/${blocking.parentId}")) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(blocking.toJson().toString())
            }.apply {
                if (status == HttpStatusCode.Created) {
                    // Update successful
                    Timber.d("Created block successfully.")

                    val element = bodyAsJson()
                        .getJSONObject("data")
                        .getJSONObject("element")
                        .let(Blocking::fromJson)
                    dataDao.insert(element)
                } else {
                    Timber.e("Could not create block in server.")
                    throw RequestException(status, bodyAsJson())
                }
            }
        }

        fun updateBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            dataDao.update(blocking)
        }

        fun deleteBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
            dataDao.notifyDeletion(
                LocalDeletion(type = "block", deleteId = blocking.id)
            )
            dataDao.delete(blocking)
        }

        /**
         * Toggles the favorite status of the currently loaded sector. Does nothing if no sector
         * is loaded.
         */
        fun toggleSectorFavorite() {
            val sector = _sector.value ?: return

            viewModelScope.launch(Dispatchers.IO) {
                userDao.toggleFavorite(sector)
            }
        }


        sealed class Selection {
            data class Index(val index: Int) : Selection()

            data object SectorInformation : Selection()
        }

        @Suppress("UNCHECKED_CAST")
        class Factory(private val sectorId: Long) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])

                return Model(application, sectorId) as T
            }
        }
    }

    sealed class Filter {
        data class Grade(
            val grades: List<GradeValue>
        ) : Filter()
    }
}
