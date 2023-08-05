package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T: Any> FormListCreator(
    list: List<T>,
    inputContent: @Composable RowScope.() -> Unit,
    rowContent: @Composable LazyItemScope.(item: T) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            inputContent()
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .padding(bottom = 8.dp)
        ) {
            items(list, itemContent = rowContent)
        }
    }
}
