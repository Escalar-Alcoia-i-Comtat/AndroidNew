package org.escalaralcoiaicomtat.android.utils

import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable

fun <S: JsonSerializable> FormBuilder.appendSerializable(key: String, obj: S) =
    append(key, obj.toJson().toString())
