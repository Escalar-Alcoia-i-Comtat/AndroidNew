package org.escalaralcoiaicomtat.android.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import org.escalaralcoiaicomtat.android.ui.list.PathItem
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class
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

        setContentThemed {
            BackInvokeHandler(onBack = ::onBack)

            val sector by viewModel.sector.observeAsState()
            val paths by viewModel.paths.observeAsState()

            Scaffold(
                topBar = {
                    AnimatedVisibility(visible = sector != null) {
                        TopAppBar(
                            title = { Text(sector?.displayName ?: "") },
                            navigationIcon = {
                                IconButton(
                                    onClick = ::onBack
                                ) {
                                    Icon(
                                        Icons.Rounded.ChevronLeft,
                                        stringResource(R.string.action_back)
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

        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
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

    private fun onBack() {
        setResult(Activity.RESULT_CANCELED)
        finish()
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
}
