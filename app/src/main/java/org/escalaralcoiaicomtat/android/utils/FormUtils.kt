package org.escalaralcoiaicomtat.android.utils

import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable

fun <S: JsonSerializable> FormBuilder.appendSerializable(key: String, obj: S) =
    append(key, obj.toJson().toString())

fun <S: JsonSerializable> FormBuilder.appendSerializableList(key: String, list: Iterable<S>) =
    append(key, list.toList().toJson().toString())

fun FormBuilder.appendULong(key: String, value: ULong) = append(key, value.toInt())

fun <E: Enum<E>> FormBuilder.appendEnum(key: String, value: E) = append(key, value.name)
