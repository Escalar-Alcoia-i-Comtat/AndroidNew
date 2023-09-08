package org.escalaralcoiaicomtat.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

object UriUtils {
    fun Context.getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        .takeIf { it >= 0 }
                        ?.let(cursor::getString)
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfter('/')
        }
        return result
    }

    /**
     * Provides a new [Intent] with [Intent.ACTION_VIEW] as action, and this uri as data.
     */
    val Uri.viewIntent: Intent get() = Intent(Intent.ACTION_VIEW, this)
}
