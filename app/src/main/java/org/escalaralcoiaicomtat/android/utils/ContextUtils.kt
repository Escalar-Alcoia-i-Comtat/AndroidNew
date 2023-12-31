package org.escalaralcoiaicomtat.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

@Volatile
private var lastToast: Toast? = null

/**
 * Creates and shows a new toast message with the contents of the string stored at [textRes] for the
 * given duration. Returns the created toast.
 *
 * @param textRes The string resource of the text to show.
 * @param duration The toast's duration. Must be one of [Toast.LENGTH_SHORT] or [Toast.LENGTH_LONG].
 * @param overrideLast If `true`, if another toast has been shown before this, it will be dismissed.
 */
fun Context.toast(
    @StringRes textRes: Int,
    @ToastDuration duration: Int = Toast.LENGTH_SHORT,
    overrideLast: Boolean = true
): Toast =
    Toast.makeText(this, textRes, duration)
        .also {
            if (overrideLast) {
                lastToast?.cancel()
                lastToast = null
            }
        }
        .also { lastToast = it }
        .also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                it.addCallback(
                    object : Toast.Callback() {
                        override fun onToastHidden() {
                            super.onToastHidden()
                            lastToast = null
                            it.removeCallback(this)
                        }
                    }
                )
            }
        }
        .also { it.show() }

/**
 * Tries opening the given url in the default browser.
 *
 * @return The launched [Intent]. Already started, no need to call [Context.startActivity]
 */
fun Context.launchUrl(url: String): Intent {
    val uri = if (!url.startsWith("http")) {
        Uri.parse("https://$url")
    } else {
        Uri.parse(url)
    }

    return Intent(Intent.ACTION_VIEW, uri).also {
        startActivity(it)
    }
}
