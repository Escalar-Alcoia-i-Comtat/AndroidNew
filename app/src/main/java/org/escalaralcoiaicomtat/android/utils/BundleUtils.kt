package org.escalaralcoiaicomtat.android.utils

import android.os.Bundle

fun Bundle.toMap(): Map<String, Any> = keySet().associateWith { get(it)!! }
