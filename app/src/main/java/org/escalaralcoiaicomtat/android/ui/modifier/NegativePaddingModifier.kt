package org.escalaralcoiaicomtat.android.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

fun Modifier.negativePadding(start: Int = 0, end: Int = 0) = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.offset((-start -end).dp.roundToPx())
    )

    layout(
        placeable.width + (start + end).dp.roundToPx(),
        placeable.height
    ) {
        placeable.place(0 + start.dp.roundToPx(), 0)
    }
}
