package org.escalaralcoiaicomtat.android.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.escalaralcoiaicomtat.android.R

@Composable
fun CreateCard(
    imageHeight: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(ListItemDefaults.HeaderHeight)
        ) {
            Text(
                text = stringResource(R.string.list_item_create),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 20.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
