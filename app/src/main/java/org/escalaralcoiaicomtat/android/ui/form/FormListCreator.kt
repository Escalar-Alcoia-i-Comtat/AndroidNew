package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import org.escalaralcoiaicomtat.android.utils.letIfNotNull

@ExperimentalFoundationApi
@Composable
fun <T: Any> FormListCreator(
    list: List<T>,
    inputContent: @Composable RowScope.() -> Unit,
    rowContent: @Composable RowScope.(index: Int, item: T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    inputHeadline: (@Composable RowScope.() -> Unit)? = null,
    rowHeadline: (@Composable RowScope.() -> Unit)? = null,
    reorderableState: (@Composable (LazyListState) -> ReorderableLazyListState)? = null
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = reorderableState?.invoke(lazyListState)

    OutlinedCard(modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            if (inputHeadline != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    inputHeadline()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                inputContent()
            }
        }
        Divider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .padding(bottom = 8.dp)
                .padding(horizontal = 4.dp)
                .letIfNotNull(reorderableLazyListState) { reorderable(it) }
                .letIfNotNull(reorderableLazyListState) { detectReorderAfterLongPress(it) },
            state = lazyListState
        ) {
            if (rowHeadline != null) {
                item(key = "headline") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowHeadline()
                    }
                }
            }
            itemsIndexed(
                items = list,
                key = { _, v -> v }
            ) { index, item ->
                if (reorderableLazyListState != null) {
                    ReorderableItem(
                        reorderableState = reorderableLazyListState,
                        key = item,
                        index = index
                    ) { isDragging ->
                        val elevation = animateDpAsState(
                            targetValue = if (isDragging) 16.dp else 0.dp,
                            label = "animate-elevation-when-dragging"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .animateItemPlacement()
                                .shadow(elevation.value),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowContent(this, index, item)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .animateItemPlacement(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowContent(this, index, item)
                    }
                }
            }
        }
    }
}
