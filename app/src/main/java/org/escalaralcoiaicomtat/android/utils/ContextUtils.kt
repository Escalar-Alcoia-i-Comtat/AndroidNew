package org.escalaralcoiaicomtat.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

/**
 * Creates and shows a new toast message with the contents of the string stored at [textRes] for the
 * given duration. Returns the created toast.
 */
fun Context.toast(
    @StringRes textRes: Int,
    @ToastDuration duration: Int = Toast.LENGTH_SHORT
): Toast =
    Toast.makeText(this, textRes, duration)
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
