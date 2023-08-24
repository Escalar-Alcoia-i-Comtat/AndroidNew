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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.storage.files.LocalFile
import org.escalaralcoiaicomtat.android.storage.files.LocalFile.Companion.file
import org.escalaralcoiaicomtat.android.storage.type.GradeValue
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.storage.type.color
import org.escalaralcoiaicomtat.android.ui.list.PathItem
import org.escalaralcoiaicomtat.android.ui.reusable.CardWithIconAndTitle
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import org.escalaralcoiaicomtat.android.ui.reusable.DropdownChip
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
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

    private val viewModel: Model by viewModels()

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

        viewModel.loadSector(sectorId)

        onBackPressedDispatcher.addCallback(this) {
            setResult(Activity.RESULT_CANCELED)
            finish()
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
                    AnimatedVisibility(visible = sector != null) {
                        TopAppBar(
                            title = { Text(sector?.displayName ?: "") },
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
                                }
                            }
                        )
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

        var imageFile by remember { mutableStateOf<LocalFile?>(null) }
        var progress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                if (imageFile != null) return@withContext

                sector.fetchImage(
                    context,
                    null,
                    progress = { c, m -> withContext(Dispatchers.Main) { progress = c to m } }
                ).collect { file ->
                    withContext(Dispatchers.Main) { imageFile = file }
                }
            }
        }

        if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(paths, key = { _, path -> path.id }) { index, path ->
                    PathItem(path)

                    if (index < paths.lastIndex) Divider()
                }
            }
        }

        imageFile?.let { file ->
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
                            .file(file)
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
                    BottomPathsView(paths)
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
    fun BottomPathsView(paths: List<Path>) {
        var selectedPath by remember { mutableStateOf<Path?>(null) }

        AnimatedContent(
            targetState = selectedPath,
            label = "paths-list"
        ) { path ->
            if (path == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(.35f)
                ) {
                    items(
                        items = paths,
                        key = { path -> path.id }
                    ) { path ->
                        PathItem(path) {
                            selectedPath = path
                        }
                    }
                }
            } else {
                OutlinedCard(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    modifier = Modifier.fillMaxHeight(.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { selectedPath = null }) {
                            Icon(Icons.Rounded.Close, stringResource(R.string.action_close))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
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
                        path.stringCount?.let { stringCount ->
                            CardWithIconAndTitle(
                                iconRes = R.drawable.climbing_anchor,
                                title = stringResource(R.string.path_view_strings_count_title),
                                message = stringResource(
                                    R.string.path_view_strings_count_message,
                                    stringCount
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
        }
    }

    class Model(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val dao = database.dataDao()

        private val _sector = MutableLiveData<Sector>()
        val sector: LiveData<Sector?> get() = _sector

        private val _paths = MutableLiveData<List<Path>>()
        val paths: LiveData<List<Path>> get() = _paths

        fun loadSector(sectorId: Long) = viewModelScope.launch(Dispatchers.IO) {
            val sector = dao.getSector(sectorId)
                ?: throw IllegalArgumentException("Could not find sector with id $sectorId")
            _sector.postValue(sector)

            val paths = dao.getPathsFromSector(sector.id)
                ?: throw IllegalArgumentException("Could not find associated paths with sector")
            _paths.postValue(paths.paths.sorted())
        }
    }

    sealed class Filter {
        data class Grade(
            val grades: List<GradeValue>
        ) : Filter()
    }
}
