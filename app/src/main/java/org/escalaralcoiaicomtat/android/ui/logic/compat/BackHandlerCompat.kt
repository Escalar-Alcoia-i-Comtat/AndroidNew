package org.escalaralcoiaicomtat.android.ui.logic.compat

import android.os.Build
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler

@Composable
fun BackHandlerCompat(
    enabled: Boolean = true,
    priority: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        OnBackInvokedDispatcher.PRIORITY_DEFAULT
    else
        -1,
    onBack: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        BackInvokeHandler(enabled, priority, onBack)
    } else {
        BackHandler(enabled, onBack)
    }
}
