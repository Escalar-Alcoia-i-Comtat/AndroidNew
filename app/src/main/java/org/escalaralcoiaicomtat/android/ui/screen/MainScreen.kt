package org.escalaralcoiaicomtat.android.ui.screen

import android.app.Activity
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.pages.SettingsPage
import org.escalaralcoiaicomtat.android.ui.reusable.ActionsFloatingActionButton
import org.escalaralcoiaicomtat.android.ui.reusable.FloatingActionButtonAction
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

    object NavigationHome : NavigationItem(
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

    object NavigationFavorites : NavigationItem(
        route = "favorites",
        label = ILabel { stringResource(R.string.item_favorites) },
        activeIcon = Icons.Filled.Bookmark,
        defaultIcon = Icons.Outlined.BookmarkBorder
    )

    object NavigationSettings : NavigationItem(
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
    onCreateOrEdit: (ImageEntity?, ImageEntity?) -> Unit,
    onSectorView: (Sector) -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(
            navController,
            onSectorView
        )
    )
) {
    val context = LocalContext.current

    val selectionWithCurrentDestination by viewModel.selectionWithCurrentDestination.observeAsState(
        null to null
    )
    val (currentSelection, currentBackStackEntry) = selectionWithCurrentDestination

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

    val favoriteAreas by viewModel.favoriteAreas.observeAsState(initial = emptyList())
    val favoriteZones by viewModel.favoriteZones.observeAsState(initial = emptyList())
    val favoriteSectors by viewModel.favoriteSectors.observeAsState(initial = emptyList())

    var backProgress by remember { mutableStateOf<Float?>(null) }

    fun onBack() {
        backProgress = null

        if (navController.previousBackStackEntry != null) {
            navController.navigateUp()
        } else (context as? Activity)?.apply {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    BackInvokeHandler(
        onBackStarted = { backProgress = it.progress },
        onBackProgressed = { backProgress = it.progress },
        onBackCancelled = { backProgress = null },
        onBack = ::onBack
    )

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

    val creationOptionsList by viewModel.creationOptionsList.observeAsState()
    creationOptionsList?.let { list ->
        AlertDialog(
            onDismissRequest = viewModel::dismissChooser,
            title = { Text(text = stringResource(R.string.new_element_choose_title)) },
            text = {
                Column {
                    var search by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text(stringResource(R.string.search)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, stringResource(R.string.search))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = list.filter { it.displayName.contains(search, true) },
                            key = { it.id },
                            contentType = { it::class.simpleName }
                        ) { data ->
                            ListItem(
                                headlineContent = { Text(data.displayName) },
                                modifier = Modifier.clickable {
                                    viewModel.pendingCreateOperation.value?.invoke(data)
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::dismissChooser
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }

    NavigationScaffold(
        items = listOfNotNull(
            Routes.NavigationHome,
            Routes.NavigationFavorites.takeIf {
                favoriteAreas.isNotEmpty() || favoriteZones.isNotEmpty() || favoriteSectors.isNotEmpty()
            },
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
                        IconButton(onClick = ::onBack) {
                            Icon(
                                Icons.Rounded.ChevronLeft,
                                stringResource(R.string.action_back)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (apiKey != null) {
                var toggled by remember { mutableStateOf(false) }

                ActionsFloatingActionButton(
                    icon = Icons.Rounded.Add,
                    actions = listOf(
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.LocationCity,
                            text = stringResource(R.string.new_area_title)
                        ) { onCreateOrEdit(null, null) },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.Map,
                            text = stringResource(R.string.new_zone_title)
                        ) {
                            viewModel.createChooser(Area::class) { onCreateOrEdit(it, null) }
                        },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.PinDrop,
                            text = stringResource(R.string.new_sector_title)
                        ) {
                            viewModel.createChooser(Zone::class) { onCreateOrEdit(it, null) }
                        },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.Route,
                            text = stringResource(R.string.new_path_title)
                        ) {
                            viewModel.createChooser(Sector::class) { onCreateOrEdit(it, null) }
                        }
                    ),
                    toggled = toggled,
                    onToggle = { toggled = !toggled }
                )
            }
        },
        pageTransition = {
            if (
                Routes.NavigationHome.equals(initialState.destination) &&
                Routes.NavigationHome.equals(targetState.destination)
            ) {
                // Navigating inside Home
                val initialAreaId = initialState.arguments?.getString(AreaId)?.toLongOrNull()
                val initialZoneId = initialState.arguments?.getString(ZoneId)?.toLongOrNull()
                val targetAreaId = targetState.arguments?.getString(AreaId)?.toLongOrNull()
                val targetZoneId = targetState.arguments?.getString(ZoneId)?.toLongOrNull()

                when {
                    (
                        // Going from Area to Zone
                        (initialZoneId == null && targetZoneId != null) ||
                            // Going from root to Area
                            (initialAreaId == null && targetAreaId != null)
                        ) -> slideInHorizontally { it } to slideOutHorizontally { -it }

                    (
                        // Going from Zone to Area
                        (initialZoneId != null && targetZoneId == null) ||
                            // Going from Area to root
                            (initialAreaId != null && targetAreaId == null)
                        ) -> slideInHorizontally { -it } to slideOutHorizontally { it }

                    else -> null
                }
            } else if (
                Routes.NavigationHome.equals(initialState.destination) &&
                Routes.NavigationSettings.equals(targetState.destination)
            ) {
                // Navigating from Home to Settings
                slideInHorizontally { it } to slideOutHorizontally { -it }
            } else if (
                Routes.NavigationSettings.equals(initialState.destination) &&
                Routes.NavigationHome.equals(targetState.destination)
            ) {
                // Navigating from Settings to Home
                slideInHorizontally { -it } to slideOutHorizontally { it }
            } else {
                null
            }
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
                    backProgress,
                    onFavoriteToggle,
                    onCreateOrEdit,
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

            Routes.NavigationFavorites -> {
                val favorites: List<ImageEntity> = listOf(favoriteAreas, favoriteZones, favoriteSectors).flatten()

                DataList(
                    list = favorites,
                    childCount = { 0U },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onClick = { viewModel.navigate(it) },
                    onFavoriteToggle = onFavoriteToggle,
                    onMove = null // Favorites cannot be reordered
                )
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
            onCreateOrEdit = { _, _ -> },
            onSectorView = {}
        )
    }
}
