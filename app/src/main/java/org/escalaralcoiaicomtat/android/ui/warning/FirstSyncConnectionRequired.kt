package org.escalaralcoiaicomtat.android.ui.warning

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.theme.AppTheme

@Composable
fun FirstSyncConnectionRequired(modifier: Modifier = Modifier) {
    OutlinedCard(modifier) {
        Text(
            text = stringResource(R.string.first_sync_network_required_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.first_sync_network_required_message),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 8.dp)
        )
    }
}

@Preview
@Composable
fun FirstSyncConnectionRequired_Preview() {
    AppTheme {
        FirstSyncConnectionRequired()
    }
}
