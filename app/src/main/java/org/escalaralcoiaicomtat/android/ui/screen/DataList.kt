package org.escalaralcoiaicomtat.android.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import org.burnoutcrew.reorderable.NoDragCancelledAnimation
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.ui.list.CreateCard
import org.escalaralcoiaicomtat.android.ui.list.DataCard
import org.escalaralcoiaicomtat.android.utils.letIf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : DataEntity> DataList(
    list: List<T>?,
    gridCellSize: Dp,
    imageHeight: Dp,
    modifier: Modifier = Modifier,
    onFavoriteToggle: (T) -> Job,
    onCreate: () -> Unit,
    onClick: (T) -> Unit,
    onMove: ((from: Int, to: Int) -> Unit)?,
    vararg sortedBy: (T) -> Comparable<*>?
) {
    val context = LocalContext.current

    val apiKey by Preferences.getApiKey(context).collectAsState(initial = null)

    if (list == null) {
        Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        val state = rememberReorderableLazyGridState(
            dragCancelledAnimation = NoDragCancelledAnimation(),
            onMove = { from, to -> onMove?.invoke(from.index, to.index) }
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = gridCellSize
            ),
            modifier = modifier.letIf(onMove != null) { it.reorderable(state) },
            state = state.gridState
        ) {
            if (list.isEmpty()) {
                item {
                    Text("No items available")
                }
            }
            itemsIndexed(
                items = list.sortedWith(
                    compareBy(
                        *sortedBy,
                        { if (it.isFavorite) "\u0000${it.displayName}" else it.displayName }
                    )
                ),
                key = { _, area -> area.id }
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
                        imageHeight = imageHeight,
                        onFavoriteToggle = { onFavoriteToggle(item) },
                        onClick = { onClick(item) }
                    )
                }
            }
            if (apiKey != null) {
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
