package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.reorderable
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.dialog.DialogScope
import org.escalaralcoiaicomtat.android.utils.letIfNotNull

@Composable
@ExperimentalFoundationApi
fun <T : Any> FormListCreator(
    list: List<T>,
    dialog: @Composable DialogScope.(indexItem: Pair<Int, T>?) -> Unit,
    rowContent: @Composable RowScope.(index: Int, item: T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    rowHeadline: (@Composable RowScope.() -> Unit)? = null,
    reorderableState: (@Composable (LazyListState) -> ReorderableLazyListState)? = null
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = reorderableState?.invoke(lazyListState)

    var editing by remember { mutableStateOf<Pair<Int, T>?>(null) }
    var showingDialog by remember { mutableStateOf(false) }
    if (showingDialog || editing != null) {
        AlertDialog(
            onDismissRequest = {
                showingDialog = false
                editing = null
            },
            text = {
                Column {
                    dialog(
                        DialogScope.create(
                            onDismiss = {
                                showingDialog = false
                                editing = null
                            }
                        ),
                        editing
                    )
                }
            },
            confirmButton = { }
        )
    }

    OutlinedCard(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                IconButton(
                    onClick = { showingDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.action_add)
                    )
                }
            }
        }
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .padding(bottom = 8.dp, start = 4.dp)
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
                                .shadow(elevation.value)
                                .clickable { editing = index to item },
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
                            .animateItemPlacement()
                            .clickable { editing = index to item },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowContent(this, index, item)
                    }
                }
            }
        }
    }
}
