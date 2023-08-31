package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class FloatingActionButtonAction(
    val icon: ImageVector,
    val text: String,
    val contentDescription: String? = text,
    val onClick: () -> Unit
)

@Composable
fun FloatingActionButtonActionRow(action: FloatingActionButtonAction) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = action.text,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        SmallFloatingActionButton(onClick = action.onClick) {
            Icon(action.icon, action.contentDescription)
        }
    }
}

@Composable
fun ActionsFloatingActionButton(
    icon: ImageVector,
    actions: List<FloatingActionButtonAction>,
    toggled: Boolean,
    onToggle: () -> Unit,
    contentDescription: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = toggled,
            label = "create-actions",
            modifier = Modifier.padding(bottom = 8.dp),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                actions.forEach { action ->
                    FloatingActionButtonActionRow(action)
                }
            }
        }
        FloatingActionButton(
            onClick = onToggle
        ) {
            AnimatedContent(
                targetState = toggled,
                label = "create-icon"
            ) { isToggled ->
                Icon(
                    imageVector = if(isToggled)
                        Icons.Rounded.Close
                    else
                        icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.rotate(if (isToggled) 90f else 0f)
                )
            }
        }
    }
}

@Preview
@Composable
fun FloatingActionButtonActionRow_Preview() {
    FloatingActionButtonActionRow(
        FloatingActionButtonAction(
            icon = Icons.Rounded.Add,
            text = "Testing Action"
        ) { }
    )
}

@Preview
@Composable
fun ActionsFloatingActionButton_Preview_NotToggled() {
    ActionsFloatingActionButton(
        icon = Icons.Rounded.Add,
        actions = emptyList(),
        toggled = false,
        onToggle = {}
    )
}

@Preview
@Composable
fun ActionsFloatingActionButton_Preview_Toggled() {
    ActionsFloatingActionButton(
        icon = Icons.Rounded.Add,
        actions = listOf(
            FloatingActionButtonAction(
                icon = Icons.Rounded.Add,
                text = "Testing Action"
            ) { },
            FloatingActionButtonAction(
                icon = Icons.Rounded.Add,
                text = "Another Action"
            ) { },
            FloatingActionButtonAction(
                icon = Icons.Rounded.Add,
                text = "Short"
            ) { }
        ),
        toggled = true,
        onToggle = {}
    )
}
