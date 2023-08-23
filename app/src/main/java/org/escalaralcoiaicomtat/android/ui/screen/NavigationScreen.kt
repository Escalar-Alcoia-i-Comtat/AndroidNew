package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.Job
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.ui.reusable.SideNavigationItem
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel

@Composable
fun NavigationScreen(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    onFavoriteToggle: (DataEntity) -> Job,
    onCreateArea: () -> Unit,
    onCreateZone: (Area) -> Unit,
    onCreateSector: (Zone) -> Unit,
    onCreatePath: (Sector) -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

    val database = AppDatabase.getInstance(context.applicationContext)
    val dao = database.dataDao()
    val areas by dao.getAllAreasLive().observeAsState()
    val zones by dao.getAllZonesLive().observeAsState(initial = emptyList())
    val sectors by dao.getAllSectorsLive().observeAsState(initial = emptyList())

    val currentSelection by viewModel.selection.observeAsState()

    val isRunningSync by viewModel.isRunningSync.observeAsState(initial = true)

    PermanentNavigationDrawer(
        drawerContent = {
            // Show only on tablets
            if (widthSizeClass != WindowWidthSizeClass.Expanded)
                return@PermanentNavigationDrawer

            PermanentDrawerSheet(
                modifier = Modifier
                    .width(if (currentSelection != null) 380.dp else 0.dp)
                    .padding(top = 12.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
            ) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.item_home)) },
                    selected = false,
                    onClick = {
                        navController.navigate(
                            Routes.NavigationHome.createRoute()
                        ) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                    icon = { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.action_back)) }
                )

                areas?.sorted()?.forEach { area ->
                    SideNavigationItem(
                        label = area.displayName,
                        depth = 0,
                        selected = area == currentSelection,
                        showCreate = apiKey != null,
                        onClick = { viewModel.navigate(area) },
                        onCreate = { onCreateZone(area) }
                    )
                    zones.takeIf { /* todo - currentArea == area */ true }
                        ?.sorted()
                        ?.filter { it.areaId == area.id }
                        ?.forEach { zone ->
                            SideNavigationItem(
                                label = zone.displayName,
                                depth = 1,
                                selected = zone == currentSelection,
                                showCreate = apiKey != null,
                                onClick = { viewModel.navigate(zone) },
                                onCreate = { onCreateSector(zone) }
                            )
                            sectors.takeIf { /* todo - currentZone == zone */ true }
                                ?.sorted()
                                ?.filter { it.zoneId == zone.id }
                                ?.forEach { sector ->
                                    SideNavigationItem(
                                        label = sector.displayName,
                                        depth = 2,
                                        selected = sector == currentSelection,
                                        showCreate = apiKey != null,
                                        onClick = { viewModel.navigate(sector) },
                                        onCreate = { onCreatePath(sector) }
                                    )
                                }
                        }
                }
            }
        }
    ) {
        AnimatedVisibility(visible = isRunningSync) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(999f)
            )
        }

        // Areas List
        AnimatedContent(
            targetState = currentSelection,
            label = "animate-areas-navigation",
            transitionSpec = {
                slideInHorizontally { if (targetState == null) -it else it } togetherWith
                    slideOutHorizontally { if (targetState == null) it else -it }
            }
        ) { selection ->
            if (selection == null) {
                DataList(
                    areas,
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onFavoriteToggle,
                    onCreateArea,
                    onClick = { viewModel.navigate(it) },
                    onMove = null // Areas cannot be reordered
                )
            }
        }
        // Zones List
        AnimatedContent(
            targetState = currentSelection,
            label = "animate-area-navigation",
            transitionSpec = {
                slideInHorizontally { if (targetState == null) -it else it } togetherWith
                    slideOutHorizontally { if (targetState == null) it else -it }
            }
        ) { selection ->
            if (selection is Area) {
                val areaWithZones by dao.getZonesFromAreaLive(selection.id).observeAsState()

                DataList(
                    list = areaWithZones?.zones,
                    gridCellSize = 210.dp,
                    imageHeight = 270.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onFavoriteToggle,
                    onCreate = { onCreateZone(selection) },
                    onClick = { viewModel.navigate(it) },
                    onMove = null // Zones cannot be reordered
                )
            }
        }
        // Sectors List
        AnimatedContent(
            targetState = currentSelection,
            label = "animate-zone-navigation",
            transitionSpec = {
                slideInHorizontally { if (targetState == null) -it else it } togetherWith
                    slideOutHorizontally { if (targetState == null) it else -it }
            }
        ) { selection ->
            if (selection is Zone) {
                val sectorsFromZone by dao.getSectorsFromZoneLive(selection.id).observeAsState()

                DataList(
                    list = sectorsFromZone?.sectors,
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onFavoriteToggle,
                    onCreate = { onCreateSector(selection) },
                    onClick = { viewModel.navigate(it) },
                    onMove = { from, to -> viewModel.moveSector(from, to) }
                )
            }
        }
    }
}
