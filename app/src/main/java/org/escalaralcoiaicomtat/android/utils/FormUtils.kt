package org.escalaralcoiaicomtat.android.utils

import io.ktor.client.request.forms.FormBuilder
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable

fun <S: JsonSerializable> FormBuilder.appendSerializable(key: String, obj: S) =
    append(key, obj.toJson().toString())

fun <S: JsonSerializable> FormBuilder.appendSerializableList(key: String, list: Iterable<S>) =
    append(key, list.toList().toJson().toString())

fun FormBuilder.appendULong(key: String, value: ULong) = append(key, value.toInt())

fun <E: Enum<E>> FormBuilder.appendEnum(key: String, value: E) = append(key, value.name)

fun <T> FormBuilder.appendDifference(key: String, new: T?, current: T?) {
    if (new == null) return
    if (new == current) return

    when (new) {
        is String -> append(key, new)
        is Number -> append(key, new)
        is Boolean -> append(key, new)
        is Enum<*> -> append(key, new.name)
        is JsonSerializable -> appendSerializable(key, new)
        is Iterable<*> -> {
            if (new.firstOrNull() is JsonSerializable) {
                @Suppress("UNCHECKED_CAST")
                appendSerializableList(key, new as Iterable<JsonSerializable>)
            } else {
                appendSerializableList(key, emptyList())
            }
        }
        is ULong -> appendULong(key, new)
    }
}
