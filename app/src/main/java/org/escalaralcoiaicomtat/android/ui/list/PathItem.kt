package org.escalaralcoiaicomtat.android.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.storage.type.color
import java.time.Instant

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LazyItemScope.PathItem(path: Path) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateItemPlacement()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = path.sketchId.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = path.displayName,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = path.grade?.displayName ?: "",
                color = path.grade.color.color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(end = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PathItem_Preview() {
    LazyColumn {
        item {
            PathItem(
                path = Path(
                    0L,
                    Instant.now(),
                    "Example Path",
                    1,
                    30,
                    SportsGrade.G6C_PLUS,
                    Ending.CHAIN_CARABINER,
                    emptyList(),
                    10,
                    10,
                    0,
                    0,
                    0,
                    0,
                    crackerRequired = false,
                    friendRequired = false,
                    lanyardRequired = false,
                    nailRequired = false,
                    pitonRequired = false,
                    stapesRequired = false,
                    showDescription = false,
                    description = "",
                    builder = null,
                    reBuilder = null,
                    sectorId = 0
                )
            )
        }
    }
}
