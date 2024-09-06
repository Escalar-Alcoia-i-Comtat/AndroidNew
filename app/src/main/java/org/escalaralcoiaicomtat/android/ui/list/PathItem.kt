package org.escalaralcoiaicomtat.android.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.type.Ending
import org.escalaralcoiaicomtat.android.storage.type.SportsGrade
import org.escalaralcoiaicomtat.android.storage.type.color

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LazyItemScope.PathItem(
    path: Path,
    blocks: List<Blocking>,
    apiKey: String?,
    shouldHighlight: Boolean,
    onClick: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val indication = ripple(
        // if set false, the ripple effect will not be cropped to the button.
        bounded = true,
        // if you want to spread to stop right at the edges of your targeted composable,
        // take the longest side divided by two, in this case the row width is the screen width.
        radius = configuration.screenWidthDp.dp / 2,
    )
    // used later to change Dp unit to Pixels using .toPx()
    val density = LocalDensity.current

    val shouldDisplayBlock = blocks.any { it.shouldDisplay() }

    // For showing ripple when shouldHighlight
    @Suppress("MagicNumber")
    LaunchedEffect(Unit) {
        if (shouldHighlight) {
            with(density) {
                scope.launch {
                    // wait a little bit for user to focus on the screen
                    delay(800)
                    val centerX = configuration.screenWidthDp.dp / 2
                    val centerY = 64.dp / 2
                    // simulate a press for the targeted setting tile
                    val press = PressInteraction.Press(Offset(centerX.toPx(), centerY.toPx()))
                    interactionSource.emit(press)
                    // wait a little bit for the effect to animate
                    delay(400)
                    // release the effect
                    val release = PressInteraction.Release(press)
                    interactionSource.emit(release)
                }
            }
        }
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .animateItemPlacement()
            .indication(interactionSource, indication),
        colors = if (blocks.isNotEmpty() && (shouldDisplayBlock || apiKey != null))
            CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        else
            CardDefaults.outlinedCardColors()
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
                modifier = Modifier.weight(1f),
                fontStyle = if (blocks.isNotEmpty() && !shouldDisplayBlock && apiKey != null)
                    FontStyle.Italic
                else
                    FontStyle.Normal
            )
            Text(
                text = path.grade?.displayName ?: "",
                color = path.grade.color.current,
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
                    parentId = 0
                ),
                blocks = emptyList(),
                shouldHighlight = false,
                apiKey = null
            ) {}
        }
    }
}
