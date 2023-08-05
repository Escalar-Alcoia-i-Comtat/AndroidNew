package org.escalaralcoiaicomtat.android.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

/**
 * Creates and shows a new toast message with the contents of the string stored at [textRes] for the
 * given duration. Returns the created toast.
 */
fun Context.toast(@StringRes textRes: Int, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, textRes, duration)
        .also { it.show() }
