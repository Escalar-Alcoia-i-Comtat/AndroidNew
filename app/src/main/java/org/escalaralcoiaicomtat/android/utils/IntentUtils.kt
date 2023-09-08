package org.escalaralcoiaicomtat.android.utils

import android.content.Context
import android.content.Intent

/**
 * Checks whether the given intent can be launched.
 */
fun Intent.canBeResolved(context: Context) = resolveActivity(context.packageManager) != null
