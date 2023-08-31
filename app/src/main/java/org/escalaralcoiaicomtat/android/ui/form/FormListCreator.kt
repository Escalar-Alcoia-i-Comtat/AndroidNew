package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress

@ExperimentalFoundationApi
@Composable
fun <T: Any> FormListCreator(
    list: List<T>,
    inputContent: @Composable RowScope.() -> Unit,
    rowContent: @Composable RowScope.(index: Int, item: T) -> Unit,
    modifier: Modifier = Modifier,
    inputHeadline: (@Composable RowScope.() -> Unit)? = null,
    rowHeadline: (@Composable RowScope.() -> Unit)? = null,
    reorderableState: ReorderableLazyListState? = null
) {
    OutlinedCard(modifier) {
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
                key = { i, _ -> "list$i" }
            ) { index, item ->
                if (reorderableState != null) {
                    ReorderableItem(
                        reorderableState = reorderableState,
                        key = "list$index"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .animateItemPlacement()
                                .detectReorderAfterLongPress(reorderableState),
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
