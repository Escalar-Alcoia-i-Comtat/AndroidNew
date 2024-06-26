package org.escalaralcoiaicomtat.android.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.escalaralcoiaicomtat.android.R

@Composable
fun FormKMZPicker(
    fileName: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FormFilePicker(
        fileName = fileName,
        label = stringResource(R.string.form_kmz_pick),
        modifier = modifier,
        onClick = onClick
    )
}
