package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.logic.compat.BackHandlerCompat
import org.escalaralcoiaicomtat.android.ui.pages.SettingsPage
import org.escalaralcoiaicomtat.android.ui.reusable.layout.NavigationScaffold
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationItem
import org.escalaralcoiaicomtat.android.ui.theme.AppTheme
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MainScreen(
    widthSizeClass: WindowWidthSizeClass,
    onApiKeySubmit: (key: String) -> Job,
    onFavoriteToggle: (DataEntity) -> Job,
    onCreateArea: () -> Unit,
    onCreateZone: (Area) -> Unit,
    onCreateSector: (Zone) -> Unit,
    onCreatePath: (Sector) -> Unit,
    onSectorView: (Sector) -> Unit,
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(onSectorView))
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState()

    val currentArea by viewModel.currentArea.observeAsState()
    val currentZone by viewModel.currentZone.observeAsState()

    /**
     * Contains all the logic to perform before calling [onBack]. Handles navigation between items.
     */
    fun onBackRequested() {
        when {
            currentZone != null -> viewModel.navigateTo(currentArea)
            currentArea != null -> viewModel.navigateTo(null)
            else -> onBack()
        }
    }

    BackHandlerCompat(onBack = ::onBackRequested)

    NavigationScaffold(
        items = listOf(
            NavigationItem(
                { stringResource(R.string.item_home) },
                Icons.Filled.Home,
                Icons.Outlined.Home
            ),
            NavigationItem(
                { stringResource(R.string.item_settings) },
                Icons.Filled.Settings,
                Icons.Outlined.Settings
            ),
        ),
        pagerState = pagerState,
        widthSizeClass = widthSizeClass,
        header = {
            val icon = remember {
                ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                    ?.toBitmap()
                    ?.asImageBitmap()
            }
            icon?.let {
                Image(
                    bitmap = it,
                    contentDescription = stringResource(R.string.app_name)
                )
            }
        },
        pageContentModifier = Modifier
            .widthIn(max = 1000.dp)
            .fillMaxSize(),
        topBar = {
            // Do not show on tablets
            if (widthSizeClass == WindowWidthSizeClass.Expanded) return@NavigationScaffold

            AnimatedVisibility(
                // Only show in home view
                visible = pagerState.currentPage == 0,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = currentArea,
                            label = "animate-area-name-change",
                            transitionSpec = {
                                fadeIn() with fadeOut()
                            }
                        ) { area ->
                            Text(
                                text = area?.displayName ?: stringResource(R.string.app_name)
                            )
                        }
                    },
                    navigationIcon = {
                        AnimatedVisibility(
                            visible = currentArea != null,
                            enter = slideInHorizontally { -it },
                            exit = slideOutHorizontally { -it }
                        ) {
                            IconButton(onClick = ::onBackRequested) {
                                Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.action_back))
                            }
                        }
                    }
                )
            }
        }
    ) { page ->
        when (page) {
            0 -> NavigationScreen(widthSizeClass, onFavoriteToggle, onCreateArea, onCreateZone, onCreateSector, onCreatePath, viewModel)

            1 -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
            ) {
                SettingsPage(onApiKeySubmit)
            }

            else -> Text("Page $page")
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MainScreen_Preview() {
    AppTheme {
        MainScreen(
            WindowWidthSizeClass.Compact,
            onApiKeySubmit = { CoroutineScope(Dispatchers.IO).launch { } },
            onFavoriteToggle = { CoroutineScope(Dispatchers.IO).launch { } },
            onCreateArea = {},
            onCreateZone = {},
            onCreateSector = {},
            onCreatePath = {},
            onSectorView = {},
            onBack = {}
        )
    }
}
