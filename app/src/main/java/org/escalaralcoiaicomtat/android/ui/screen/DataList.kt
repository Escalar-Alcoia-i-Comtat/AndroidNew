package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.reflect.KClass
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.NoDragCancelledAnimation
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.sorted
import org.escalaralcoiaicomtat.android.storage.type.PointOptions
import org.escalaralcoiaicomtat.android.ui.list.CreateCard
import org.escalaralcoiaicomtat.android.ui.list.DataCard
import org.escalaralcoiaicomtat.android.ui.reusable.InfoRow
import org.escalaralcoiaicomtat.android.utils.UriUtils.viewIntent
import org.escalaralcoiaicomtat.android.utils.canBeResolved
import org.escalaralcoiaicomtat.android.utils.letIf

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun <ParentType : ImageEntity, ItemType : ImageEntity> DataList(
    kClass: KClass<ItemType>,
    parent: ParentType?,
    list: List<ItemType>?,
    gridCellSize: Dp,
    imageHeight: Dp,
    crossinline childCount: (ItemType) -> UInt,
    modifier: Modifier = Modifier,
    crossinline onClick: (ItemType) -> Unit,
    crossinline onFavoriteToggle: (ItemType) -> Job,
    noinline onCreate: (() -> Unit)? = null,
    noinline onEdit: ((ItemType) -> Unit)? = null,
    noinline onMove: ((from: Int, to: Int) -> Unit)? = null,
    sortItems: Boolean = true
) {
    val context = LocalContext.current

    if (list == null) {
        Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        val scope = rememberCoroutineScope()
        val gridState = rememberLazyGridState()
        val state = rememberReorderableLazyGridState(
            dragCancelledAnimation = NoDragCancelledAnimation(),
            gridState = gridState,
            onMove = { from, to -> onMove?.invoke(from.index, to.index) }
        )

        LaunchedEffect(list) {
            scope.launch { gridState.scrollToItem(0) }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = gridCellSize
            ),
            modifier = modifier.letIf(onMove != null) { it.reorderable(state) },
            state = state.gridState
        ) {
            if (kClass == Sector::class && parent != null) {
                val zone = parent as Zone
                val zonePoint = zone.point

                item(
                    key = "zone-information"
                ) {
                    Text(
                        text = stringResource(R.string.list_zone_information_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
                if (zonePoint != null) {
                    item {
                        InfoRow(
                            icon = Icons.Rounded.Place,
                            iconContentDescription = stringResource(R.string.info_point_description),
                            title = stringResource(R.string.info_zone_location),
                            subtitle = "${zonePoint.latitude}, ${zonePoint.longitude}",
                            actions = listOfNotNull(
                                zonePoint
                                    .uri(zone.displayName)
                                    .viewIntent
                                    .apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    .takeIf { it.canBeResolved(context) }
                                    ?.let { intent ->
                                        Icons.Rounded.Map to {
                                            context.startActivity(intent)
                                        }
                                    }
                            )
                        )
                    }
                }
                for (point in zone.points) {
                    item {
                        val pointOption = PointOptions.valueOf(point.icon)
                        val pointLocation = point.location

                        InfoRow(
                            icon = pointOption?.icon ?: Icons.Rounded.Place,
                            iconContentDescription = point.label,
                            title = point.label,
                            subtitle = "${pointLocation.latitude}, ${pointLocation.longitude}",
                            actions = listOfNotNull(
                                pointLocation
                                    .uri(point.label)
                                    .viewIntent
                                    .apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    .takeIf { it.canBeResolved(context) }
                                    ?.let { intent ->
                                        Icons.Rounded.Map to {
                                            context.startActivity(intent)
                                        }
                                    }
                            )
                        )
                    }
                }
            }
            if (parent != null) {
                item(
                    key = "children-list",
                    span = { GridItemSpan(maxCurrentLineSpan) }
                ) {
                    Text(
                        text = stringResource(parent.childrenTitleRes),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(bottom = 4.dp, top = 8.dp)
                            .padding(horizontal = 8.dp)
                    )
                }
            }
            if (list.isEmpty() && onCreate == null) {
                item {
                    // TODO - translate
                    Text("No items available")
                }
            }
            itemsIndexed(
                items = if (sortItems) list.sorted() else list,
                key = { _, item -> item.id },
                contentType = { _, item -> item::class.simpleName }
            ) { index, item ->
                ReorderableItem(
                    reorderableState = state,
                    key = item.id,
                    index = index
                ) {
                    DataCard(
                        item = item,
                        modifier = Modifier
                            .padding(12.dp)
                            .animateItemPlacement()
                            // Only detect long click if onMove is not null
                            .letIf(onMove != null) {
                                it.detectReorderAfterLongPress(state)
                            },
                        childCount = childCount(item),
                        imageHeight = imageHeight,
                        onFavoriteToggle = { onFavoriteToggle(item) },
                        onClick = { onClick(item) },
                        onEdit = onEdit?.let { { it(item) } }
                    )
                }
            }
            if (onCreate != null) {
                item(key = "create") {
                    CreateCard(
                        imageHeight = imageHeight,
                        modifier = Modifier
                            .padding(12.dp)
                            .animateItemPlacement(),
                        onClick = onCreate
                    )
                }
            }
        }
    }
}
