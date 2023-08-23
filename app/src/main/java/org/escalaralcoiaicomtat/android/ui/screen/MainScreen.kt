package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.pages.SettingsPage
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationItem
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationItem.ILabel
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationScaffold
import org.escalaralcoiaicomtat.android.ui.screen.Routes.Arguments.AreaId
import org.escalaralcoiaicomtat.android.ui.screen.Routes.Arguments.ZoneId
import org.escalaralcoiaicomtat.android.ui.theme.AppTheme
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel

object Routes {
    object Arguments {
        const val AreaId = "areaId"
        const val ZoneId = "zoneId"
    }

    object NavigationHome: NavigationItem(
        route = "home?$AreaId={$AreaId}&$ZoneId={$ZoneId}",
        root = "home",
        arguments = listOf(
            // Use String instead of Int for id since Int is not nullable in Java
            Argument(AreaId, NavType.StringType, true),
            Argument(ZoneId, NavType.StringType, true)
        ),
        label = ILabel { stringResource(R.string.item_home) },
        activeIcon = Icons.Filled.Home,
        defaultIcon = Icons.Outlined.Home
    ) {
        /**
         * Creates the route for navigating to the given ids.
         */
        fun createRoute(areaId: Long? = null, zoneId: Long? = null): String {
            val params = mutableMapOf<String, Long?>()
            areaId?.let { params[AreaId] = it }
            zoneId?.let { params[ZoneId] = it }
            return "home" + (params
                .takeIf { it.isNotEmpty() }
                ?.let { "?" + it.toList().joinToString("&") { (k, v) -> "$k=$v" } } ?: "")
        }
    }

    object NavigationSettings: NavigationItem(
        route = "settings",
        label = ILabel { stringResource(R.string.item_settings) },
        activeIcon = Icons.Filled.Settings,
        defaultIcon = Icons.Outlined.Settings
    )
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    widthSizeClass: WindowWidthSizeClass,
    onApiKeySubmit: (key: String) -> Job,
    onFavoriteToggle: (DataEntity) -> Job,
    onCreateArea: () -> Unit,
    onCreateZone: (Area) -> Unit,
    onCreateSector: (Zone) -> Unit,
    onCreatePath: (Sector) -> Unit,
    onSectorView: (Sector) -> Unit,
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(navController, onSectorView))
) {
    val context = LocalContext.current

    val selectionWithCurrentDestination by viewModel.selectionWithCurrentDestination.observeAsState(null to null)
    val (currentSelection, currentBackStackEntry) = selectionWithCurrentDestination

    /**
     * Contains all the logic to perform before calling [onBack]. Handles navigation between items.
     */
    fun onBackRequested() {
        when {
            navController.previousBackStackEntry != null -> navController.navigateUp()
            else -> onBack()
        }
    }

    BackInvokeHandler(onBack = ::onBackRequested)

    // Attach the nav controller
    viewModel.Navigation()

    // Progress bar shows over everything
    val isRunningSync by viewModel.isRunningSync.observeAsState(initial = true)

    AnimatedVisibility(visible = isRunningSync) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(999f)
        )
    }

    NavigationScaffold(
        items = listOf(
            Routes.NavigationHome,
            Routes.NavigationSettings
        ),
        initialRoute = Routes.NavigationHome.createRoute(),
        navController = navController,
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

            CenterAlignedTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = selectionWithCurrentDestination,
                        label = "animate-title-change",
                        transitionSpec = {
                            val initialData = initialState.first
                            val targetData = targetState.first

                            val initialDestination = initialState.second
                            val targetDestination = targetState.second

                            if (Routes.NavigationSettings.equals(initialDestination)) {
                                // Going to Home
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                            } else if (
                                Routes.NavigationHome.equals(initialDestination) &&
                                !Routes.NavigationHome.equals(targetDestination)
                            ) {
                                // Going from Home
                                slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                            } else if (
                            // Going back from area
                                (targetData == null && initialData is Area) ||
                                // Going back from zone to area
                                (targetData is Area && initialData is Zone) ||
                                // Going back from sector to zone
                                (targetData is Zone && initialData is Sector)
                            ) {
                                slideInVertically { it } + fadeIn() togetherWith
                                    slideOutVertically { -it } + fadeOut()
                            } else {
                                // Going forward
                                slideInVertically { -it } + fadeIn() togetherWith
                                    slideOutVertically { it } + fadeOut()
                            }.using(
                                // Disable clipping since the faded slide-in/out should
                                // be displayed out of bounds.
                                SizeTransform(clip = false)
                            )
                        }
                    ) { (selection, entry) ->
                        Text(
                            text = when {
                                Routes.NavigationSettings.equals(entry) -> stringResource(R.string.item_settings)
                                selection != null -> selection.displayName
                                else -> stringResource(R.string.app_name)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = Routes.NavigationHome.equals(currentBackStackEntry) && currentSelection != null,
                        enter = slideInHorizontally { -it },
                        exit = slideOutHorizontally { -it }
                    ) {
                        IconButton(onClick = ::onBackRequested) {
                            Icon(
                                Icons.Rounded.ChevronLeft,
                                stringResource(R.string.action_back)
                            )
                        }
                    }
                }
            )
        }
    ) { page, entry ->
        when (page) {
            Routes.NavigationHome -> {
                val areaId = entry.arguments?.getString(AreaId)?.toLongOrNull()
                val zoneId = entry.arguments?.getString(ZoneId)?.toLongOrNull()

                viewModel.load(areaId, zoneId)

                NavigationScreen(
                    navController,
                    widthSizeClass,
                    onFavoriteToggle,
                    onCreateArea,
                    onCreateZone,
                    onCreateSector,
                    onCreatePath,
                    viewModel
                )
            }
            Routes.NavigationSettings -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
            ) {
                SettingsPage(onApiKeySubmit)
            }
            else -> {}
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MainScreen_Preview() {
    AppTheme {
        MainScreen(
            widthSizeClass = WindowWidthSizeClass.Compact,
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
