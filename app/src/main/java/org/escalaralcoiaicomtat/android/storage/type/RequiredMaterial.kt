package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class RequiredMaterial(@StringRes val displayName: Int) {
    val text: String
        @Composable
        get() = stringResource(displayName)
}
