package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

enum class SizeMode {
    FIXED_WIDTH, FILL_MAX_WIDTH, WRAP
}

@Composable
fun FormSegmentedButton(
    items: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    defaultSelectedItemIndex: Int = 0,
    mode: SizeMode = SizeMode.WRAP,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 10,
    color: Color = MaterialTheme.colorScheme.primary,
    onItemSelection: (selectedItemIndex: Int) -> Unit
) {
    val selectedIndex = remember { mutableIntStateOf(defaultSelectedItemIndex) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        Row {
            items.forEachIndexed { index, item ->
                OutlinedButton(
                    modifier = when (index) {
                        0 -> when (mode) {
                            SizeMode.WRAP -> Modifier.wrapContentSize()
                            SizeMode.FIXED_WIDTH -> Modifier.width(itemWidth)
                            SizeMode.FILL_MAX_WIDTH -> Modifier.weight(1f)
                        }
                            .offset(0.dp, 0.dp)
                            .zIndex(if (selectedIndex.value == 0) 1f else 0f)

                        else -> when (mode) {
                            SizeMode.WRAP -> Modifier.wrapContentSize()
                            SizeMode.FIXED_WIDTH -> Modifier.width(itemWidth)
                            SizeMode.FILL_MAX_WIDTH -> Modifier.weight(1f)
                        }
                            .offset((-1 * index).dp, 0.dp)
                            .zIndex(if (selectedIndex.value == index) 1f else 0f)
                    },
                    onClick = {
                        selectedIndex.value = index
                        onItemSelection(selectedIndex.value)
                    },
                    shape = when (index) {
                        /**
                         * left outer button
                         */
                        0 -> RoundedCornerShape(
                            topStartPercent = cornerRadius,
                            topEndPercent = 0,
                            bottomStartPercent = cornerRadius,
                            bottomEndPercent = 0
                        )
                        /**
                         * right outer button
                         */
                        items.size - 1 -> RoundedCornerShape(
                            topStartPercent = 0,
                            topEndPercent = cornerRadius,
                            bottomStartPercent = 0,
                            bottomEndPercent = cornerRadius
                        )
                        /**
                         * middle button
                         */
                        else -> RoundedCornerShape(
                            topStartPercent = 0,
                            topEndPercent = 0,
                            bottomStartPercent = 0,
                            bottomEndPercent = 0
                        )
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selectedIndex.value == index) {
                            color
                        } else {
                            color.copy(alpha = 0.75f)
                        }
                    ),
                    colors = if (selectedIndex.value == index) {
                        /**
                         * selected colors
                         */
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = color
                        )
                    } else {
                        /**
                         * not selected colors
                         */
                        ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                    },
                ) {
                    Text(
                        text = item,
                        fontWeight = FontWeight.Normal,
                        color = if (selectedIndex.value == index) {
                            Color.White
                        } else {
                            color.copy(alpha = 0.9f)
                        },
                    )
                }
            }
        }
    }
}
