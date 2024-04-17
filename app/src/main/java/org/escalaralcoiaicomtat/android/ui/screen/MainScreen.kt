package org.escalaralcoiaicomtat.android.ui.screen

import android.app.Activity
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.MainActivity
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.modifier.negativePaddingVertical
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
    onCreateOrEdit: MainActivity.ICreateOrEdit<ImageEntity>,
    navigate: (target: DataEntity?) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    val selectionWithCurrentDestination by viewModel.selectionWithCurrentDestination.observeAsState(
        null to null
    )
    val (currentSelection, currentBackStackEntry) = selectionWithCurrentDestination

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

    val favorites by viewModel.favorites.observeAsState(initial = emptyList())

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

    PredictiveBackHandler { progress: Flow<BackEventCompat> ->
        // code for gesture back started
        backProgress = 0f
        try {
            progress.collect { backEvent ->
                // code for progress
                backProgress = backEvent.progress
            }
            // code for completion
            onBack()
        } catch (e: CancellationException) {
            // code for cancellation
            backProgress = null
        }
    }

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
            Routes.NavigationFavorites.takeIf { favorites.isNotEmpty() },
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

            val searchQuery by viewModel.searchQuery.collectAsState(initial = "")
            val isSearching by viewModel.isSearching.collectAsState(initial = false)
            val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())

            val searchBarFocusRequester = remember { FocusRequester() }

            LaunchedEffect(isSearching) {
                if (isSearching) searchBarFocusRequester.requestFocus()
            }
            LaunchedEffect(searchQuery) {
                if (!isSearching && searchQuery.isNotEmpty()) viewModel.isSearching.tryEmit(true)
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel.searchQuery::tryEmit,
                onSearch = { viewModel.search(searchQuery) },
                active = isSearching,
                onActiveChange = viewModel.isSearching::tryEmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchBarFocusRequester),
                shape = RectangleShape,
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background,
                    inputFieldColors = SearchBarDefaults.inputFieldColors(
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground
                    )
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                windowInsets = WindowInsets.systemBars,
                placeholder = {
                    AnimatedContent(
                        targetState = isSearching,
                        label = "animate searching label",
                        transitionSpec = {
                            slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                        }
                    ) { searching ->
                        if (searching) {
                            Text(stringResource(R.string.search))
                        } else {
                            AnimatedTitleText(selectionWithCurrentDestination)
                        }
                    }
                },
                leadingIcon = {
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
                },
                trailingIcon = {
                    SearchBarActions(isSearching, apiKey, viewModel, onCreateOrEdit)
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { entity ->
                        ListItem(
                            headlineContent = { Text(text = entity.displayName) },
                            trailingContent = {
                                Badge {
                                    Text(
                                        text = stringResource(
                                            when (entity) {
                                                is Area -> R.string.type_area
                                                is Zone -> R.string.type_zone
                                                is Sector -> R.string.type_sector
                                                else -> R.string.type_route
                                            }
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                navigate(entity)
                                viewModel.searchQuery.tryEmit("")
                                viewModel.isSearching.tryEmit(false)
                            }
                        )
                    }
                }
            }
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
                        ) { onCreateOrEdit(Area::class, null, null) },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.Map,
                            text = stringResource(R.string.new_zone_title)
                        ) {
                            viewModel.createChooser(Area::class) {
                                onCreateOrEdit(
                                    Zone::class,
                                    it.id,
                                    null
                                )
                            }
                        },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.PinDrop,
                            text = stringResource(R.string.new_sector_title)
                        ) {
                            viewModel.createChooser(Zone::class) {
                                onCreateOrEdit(
                                    Sector::class,
                                    it.id,
                                    null
                                )
                            }
                        },
                        FloatingActionButtonAction(
                            icon = Icons.Outlined.Route,
                            text = stringResource(R.string.new_path_title)
                        ) {
                            viewModel.createChooser(Sector::class) {
                                onCreateOrEdit(
                                    Path::class,
                                    it.id,
                                    null
                                )
                            }
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
                    navigate,
                    viewModel
                )
            }

            Routes.NavigationSettings -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsPage(onApiKeySubmit)
            }

            Routes.NavigationFavorites -> {
                DataList(
                    kClass = ImageEntity::class,
                    parent = null,
                    list = favorites,
                    childCount = { 0U },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onClick = navigate,
                    onFavoriteToggle = onFavoriteToggle,
                    onMove = null, // Favorites cannot be reordered
                    // Since the list mixes multiple data types, sorting is not possible
                    sortItems = false
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun AnimatedTitleText(selectionWithCurrentDestination: Pair<DataEntity?, NavDestination?>) {
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
            modifier = Modifier.fillMaxWidth().negativePaddingVertical(top = (-8).dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchBarActions(
    isSearching: Boolean,
    apiKey: String?,
    viewModel: MainViewModel,
    onCreateOrEdit: MainActivity.ICreateOrEdit<ImageEntity>
) {
    AnimatedContent(
        targetState = isSearching,
        label = "search-close-button"
    ) { searching ->
        if (searching) {
            IconButton(
                onClick = {
                    viewModel.searchQuery.tryEmit("")
                    viewModel.isSearching.tryEmit(false)
                }
            ) {
                Icon(Icons.Rounded.Close, stringResource(R.string.action_close))
            }
        } else Row {
            val selection by viewModel.selection.observeAsState()

            IconButton(
                onClick = { viewModel.isSearching.tryEmit(true) }
            ) {
                Icon(
                    Icons.Rounded.Search,
                    stringResource(R.string.search)
                )
            }

            selection?.let { data ->
                if (apiKey != null) {
                    IconButton(
                        onClick = {
                            if (selection is Area) {
                                onCreateOrEdit(Area::class, null, data)
                            } else if (selection is Zone) {
                                onCreateOrEdit(Zone::class, data.parentId, data)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            stringResource(R.string.action_edit)
                        )
                    }
                }
            }
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
            onCreateOrEdit = { _, _, _ -> },
            navigate = { }
        )
    }
}
