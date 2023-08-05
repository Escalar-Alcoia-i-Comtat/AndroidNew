package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.Job
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.reusable.SideNavigationItem
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationScreen(
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

    val currentSelection by viewModel.currentSelection.observeAsState()

    val isRunningSync by viewModel.isRunningSync.observeAsState(initial = true)

    PermanentNavigationDrawer(
        drawerContent = {
            // Show only on tablets
            if (widthSizeClass != WindowWidthSizeClass.Expanded)
                return@PermanentNavigationDrawer

            val visible = currentSelection != null

            val width by animateDpAsState(
                targetValue = if (visible) 380.dp else 0.dp,
                label = "drawer width animation"
            )

            PermanentDrawerSheet(
                modifier = Modifier
                    .width(width)
                    .padding(top = 12.dp, end = 16.dp)
            ) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.item_home)) },
                    selected = false,
                    onClick = { viewModel.clear() },
                    modifier = Modifier.padding(bottom = 12.dp),
                    icon = { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.action_back)) }
                )

                areas?.forEach { area ->
                    SideNavigationItem(
                        label = area.displayName,
                        depth = 0,
                        selected = area == currentSelection,
                        showCreate = apiKey != null,
                        onClick = { viewModel.navigateTo(area) },
                        onCreate =  { onCreateZone(area) }
                    )
                    zones.filter { it.areaId == area.id }.forEach { zone ->
                        SideNavigationItem(
                            label = zone.displayName,
                            depth = 1,
                            selected = zone == currentSelection,
                            showCreate = apiKey != null,
                            onClick = { viewModel.navigateTo(zone) },
                            onCreate =  { onCreateSector(zone) }
                        )
                        sectors.filter { it.zoneId == zone.id }.forEach { sector ->
                            SideNavigationItem(
                                label = sector.displayName,
                                depth = 2,
                                selected = sector == currentSelection,
                                showCreate = apiKey != null,
                                onClick = { viewModel.navigateTo(sector) },
                                onCreate =  { onCreatePath(sector) }
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
                slideInHorizontally { if (targetState == null) -it else it } with
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
                    onClick = { viewModel.navigateTo(it) },
                    onMove = null // Areas cannot be reordered
                )
            }
        }
        // Zones List
        AnimatedContent(
            targetState = currentSelection,
            label = "animate-area-navigation",
            transitionSpec = {
                slideInHorizontally { if (targetState == null) -it else it } with
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
                    onClick = { viewModel.navigateTo(it) },
                    onMove = null // Zones cannot be reordered
                )
            }
        }
        // Sectors List
        AnimatedContent(
            targetState = currentSelection,
            label = "animate-zone-navigation",
            transitionSpec = {
                slideInHorizontally { if (targetState == null) -it else it } with
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
                    onClick = { viewModel.navigateTo(it) },
                    onMove = { from, to -> viewModel.moveSector(from, to) },
                    ({ it.weight })
                )
            }
        }
    }
}
