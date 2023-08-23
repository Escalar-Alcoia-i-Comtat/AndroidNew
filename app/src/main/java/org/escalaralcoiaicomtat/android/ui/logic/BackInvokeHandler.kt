package org.escalaralcoiaicomtat.android.ui.logic

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun BackInvokeHandler(
    enabled: Boolean = true,
    onBackStarted: ((BackEventCompat) -> Unit)? = null,
    onBackProgressed: ((BackEventCompat) -> Unit)? = null,
    onBackCancelled: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val callback = remember {
        object : OnBackPressedCallback(enabled = enabled) {
            override fun handleOnBackPressed() {
                onBack()
            }

            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                onBackStarted?.invoke(backEvent)
            }

            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                onBackProgressed?.invoke(backEvent)
            }

            override fun handleOnBackCancelled() {
                onBackCancelled?.invoke()
            }
        }
    }

    val activity = when (lifecycleOwner) {
        is AppCompatActivity -> lifecycleOwner
        else -> {
            val context = LocalContext.current
            if (context is AppCompatActivity) {
                context
            } else {
                throw IllegalStateException("LocalLifecycleOwner is not AppCompatActivity")
            }
        }
    }

    activity.onBackPressedDispatcher.addCallback(lifecycleOwner, callback)

    LaunchedEffect(enabled) {
        callback.isEnabled = enabled
    }
}
