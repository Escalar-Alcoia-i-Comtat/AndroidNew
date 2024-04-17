package org.escalaralcoiaicomtat.android.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
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

fun Modifier.negativePaddingVertical(top: Dp = 0.dp, bottom: Dp = 0.dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(
        constraints.offset((-top -bottom).roundToPx())
    )

    layout(
        placeable.width,
        placeable.height + (top + bottom).roundToPx()
    ) {
        placeable.place(0, 0 + top.roundToPx())
    }
}
