package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardWithIconAndTitle(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    onClick: (() -> Unit)? = null,
    extra: (@Composable ColumnScope.() -> Unit)? = null
) {
    @Composable
    fun Content() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(16.dp)
                    .size(36.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall
                )
                extra?.invoke(this)
            }
        }
    }

    onClick?.let {
        OutlinedCard(
            modifier = modifier,
            colors = colors,
            border = border,
            onClick = it,
            content = { Content() }
        )
    } ?: OutlinedCard(
        modifier = modifier,
        colors = colors,
        border = border,
        content = { Content() }
    )
}
