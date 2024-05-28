package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.Job
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.MainActivity
import org.escalaralcoiaicomtat.android.network.NetworkObserver.Companion.rememberNetworkObserver
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.ui.modifier.backAnimation
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import org.escalaralcoiaicomtat.android.ui.reusable.SideNavigationItem
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel
import org.escalaralcoiaicomtat.android.ui.warning.FirstSyncConnectionRequired

@Composable
fun NavigationScreen(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
    backProgress: Float?,
    onFavoriteToggle: (DataEntity) -> Job,
    onCreateOrEdit: MainActivity.ICreateOrEdit<ImageEntity>,
    navigate: (target: DataEntity?) -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val networkObserver = rememberNetworkObserver()
    val isNetworkAvailable by networkObserver.collectIsNetworkAvailable()

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)
    val hasEverSynchronized by Preferences.hasEverSynchronized(context).collectAsState(initial = false)

    val areas by viewModel.areas.collectAsState(initial = emptyList())
    val zones by viewModel.zones.collectAsState(initial = emptyList())
    val sectors by viewModel.sectors.collectAsState(initial = emptyList())
    val paths by viewModel.paths.collectAsState(initial = emptyList())

    val selection by viewModel.selection.collectAsState()

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

                areas.sorted().forEach { area ->
                    SideNavigationItem(
                        label = area.displayName,
                        depth = 0,
                        selected = area == selection,
                        isEditable = apiKey != null,
                        onClick = { navigate(area) },
                        onCreate = { onCreateOrEdit(Zone::class, area.id, null) },
                        onEdit = { onCreateOrEdit(Area::class, null, area) }
                    )
                    zones.takeIf { selection == area || (selection as? Zone)?.parentId == area.id }
                        ?.sorted()
                        ?.filter { it.parentId == area.id }
                        ?.forEach { zone ->
                            SideNavigationItem(
                                label = zone.displayName,
                                depth = 1,
                                selected = zone == selection,
                                isEditable = apiKey != null,
                                onClick = { navigate(zone) },
                                onCreate = { onCreateOrEdit(Sector::class, zone.id, null) },
                                onEdit = { onCreateOrEdit(Zone::class, area.id, zone) }
                            )
                            sectors.takeIf { selection == zone }
                                ?.sorted()
                                ?.filter { it.parentId == zone.id }
                                ?.forEach { sector ->
                                    SideNavigationItem(
                                        label = sector.displayName,
                                        depth = 2,
                                        selected = sector == selection,
                                        isEditable = apiKey != null,
                                        onClick = { navigate(sector) },
                                        onCreate = { onCreateOrEdit(Path::class, sector.id, null) },
                                        onEdit = { onCreateOrEdit(Sector::class, zone.id, sector) }
                                    )
                                }
                        }
                }
            }
        }
    ) {
        val data = selection
        when {
            // First synchronization
            !hasEverSynchronized -> {
                // Network is required for the first synchronization
                if (isNetworkAvailable) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    FirstSyncConnectionRequired(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp)
                    )
                }
            }
            // Areas List
            data == null -> {
                DataList(
                    kClass = Area::class,
                    parent = null,
                    list = areas,
                    childCount = { area -> zones.count { it.parentId == area.id }.toUInt() },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    onClick = navigate,
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Area::class, null, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Area::class, null, it) } },
                    onMove = null // Areas cannot be reordered
                )
            }
            // Zones List
            data is Area -> {
                val areaWithZones by viewModel.areaWithZones.collectAsState(null)

                DataList(
                    kClass = Zone::class,
                    parent = data,
                    list = areaWithZones?.zones,
                    childCount = { zone -> sectors.count { it.parentId == zone.id }.toUInt() },
                    gridCellSize = 210.dp,
                    imageHeight = 300.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .backAnimation(backProgress),
                    onClick = navigate,
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Zone::class, data.id, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Zone::class, data.id, it) } },
                    onMove = null // Zones cannot be reordered
                )
            }
            // Sectors List
            data is Zone -> {
                val sectorsFromZone by viewModel.sectorsFromZone.collectAsState(null)

                DataList(
                    kClass = Sector::class,
                    parent = data,
                    list = sectorsFromZone?.sectors,
                    childCount = { sector -> paths.count { it.parentId == sector.id }.toUInt() },
                    gridCellSize = 400.dp,
                    imageHeight = 200.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                        .backAnimation(backProgress),
                    onClick = navigate,
                    onFavoriteToggle = onFavoriteToggle,
                    onCreate = apiKey?.let { { onCreateOrEdit(Sector::class, data.id, null) } },
                    onEdit = apiKey?.let { { onCreateOrEdit(Sector::class, data.id, it) } },
                    onMove = { from, to -> viewModel.moveSector(from, to) }
                )
            }
        }
    }
}
