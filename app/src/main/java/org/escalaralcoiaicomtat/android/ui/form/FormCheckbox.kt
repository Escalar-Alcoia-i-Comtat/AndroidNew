package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FormCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onCheckedChange, enabled = enabled)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FormCheckbox_PreviewNotChecked() {
    FormCheckbox(checked = false, onCheckedChange = {}, label = "Testing checkbox")
}

@Preview(showBackground = true)
@Composable
fun FormCheckbox_PreviewChecked() {
    FormCheckbox(checked = true, onCheckedChange = {}, label = "Testing checkbox")
}
