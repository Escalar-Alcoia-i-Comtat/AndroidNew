package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.Job
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.MainActivity
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.ui.modifier.backAnimation
import org.escalaralcoiaicomtat.android.ui.reusable.SideNavigationItem
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel

@Composable
fun NavigationScreen(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    backProgress: Float?,
    onFavoriteToggle: (DataEntity) -> Job,
    onCreateOrEdit: MainActivity.ICreateOrEdit<ImageEntity, ImageEntity>,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

    val database = AppDatabase.getInstance(context.applicationContext)
    val dao = database.dataDao()
    val areas by dao.getAllAreasLive().observeAsState()
    val zones by dao.getAllZonesLive().observeAsState(initial = emptyList())
    val sectors by dao.getAllSectorsLive().observeAsState(initial = emptyList())
    val paths by dao.getAllPathsLive().observeAsState(initial = emptyList())

    val selection by viewModel.selection.observeAsState()

    PermanentNavigationDrawer(
        drawerContent = {
            // Show only on tablets
            if (widthSizeClass != WindowWidthSizeClass.Expanded)
                return@PermanentNavigationDrawer

            PermanentDrawerSheet(
                modifier = Modifier
                    .width(if (selection != null) 380.dp else 0.dp)
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
                            try {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            } catch (_: NoSuchElementException) {
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
                        selected = area == selection,
                        showCreate = apiKey != null,
                        onClick = { viewModel.navigate(area) },
                        onCreate = { onCreateOrEdit(Zone::class, area, null) }
                    )
                    zones.takeIf { selection == area || (selection as? Zone)?.areaId == area.id }
                        ?.sorted()
                        ?.filter { it.areaId == area.id }
                        ?.forEach { zone ->
                            SideNavigationItem(
                                label = zone.displayName,
                                depth = 1,
                                selected = zone == selection,
                                showCreate = apiKey != null,
                                onClick = { viewModel.navigate(zone) },
                                onCreate = { onCreateOrEdit(Sector::class, zone, null) }
                            )
                            sectors.takeIf { selection == zone }
                                ?.sorted()
                                ?.filter { it.zoneId == zone.id }
                                ?.forEach { sector ->
                                    SideNavigationItem(
                                        label = sector.displayName,
                                        depth = 2,
                                        selected = sector == selection,
                                        showCreate = apiKey != null,
                                        onClick = { viewModel.navigate(sector) },
                                        onCreate = { onCreateOrEdit(Path::class, sector, null) }
                                    )
                                }
                        }
                }
            }
        }
    ) {
        when (val data = selection) {
            // Areas List
            null -> {
                DataList(
                    list = areas,
                    childCount = { area -> zones.count { it.areaId == area.id }.toUInt() },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onClick = { viewModel.navigate(it) },
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Area::class, null, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Area::class, null, it) } },
                    onMove = null // Areas cannot be reordered
                )
            }
            // Zones List
            is Area -> {
                val areaWithZones by dao.getZonesFromAreaLive(data.id).observeAsState()

                DataList(
                    list = areaWithZones?.zones,
                    childCount = { zone -> sectors.count { it.zoneId == zone.id }.toUInt() },
                    gridCellSize = 210.dp,
                    imageHeight = 300.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .backAnimation(backProgress),
                    onClick = { viewModel.navigate(it) },
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Zone::class, data, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Zone::class, data, it) } },
                    onMove = null // Zones cannot be reordered
                )
            }
            // Sectors List
            is Zone -> {
                val sectorsFromZone by dao.getSectorsFromZoneLive(data.id).observeAsState()

                DataList(
                    list = sectorsFromZone?.sectors,
                    childCount = { sector -> paths.count { it.sectorId == sector.id }.toUInt() },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .backAnimation(backProgress),
                    onClick = { viewModel.navigate(it) },
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Sector::class, data, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Sector::class, data, it) } },
                    onMove = { from, to -> viewModel.moveSector(from, to) }
                )
            }
        }
    }
}
