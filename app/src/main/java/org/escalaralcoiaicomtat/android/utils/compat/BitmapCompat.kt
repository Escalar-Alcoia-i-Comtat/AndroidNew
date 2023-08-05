package org.escalaralcoiaicomtat.android.utils.compat

import android.graphics.Bitmap
import android.os.Build

object BitmapCompat {
    @Suppress("DEPRECATION")
    val WEBP_LOSSLESS: Bitmap.CompressFormat
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            Bitmap.CompressFormat.WEBP
        }
}
