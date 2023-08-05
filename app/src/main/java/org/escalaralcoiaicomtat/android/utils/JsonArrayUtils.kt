package org.escalaralcoiaicomtat.android.utils

import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONArray


/**
 * Parses the string as a JSON array and returns the result as a JSONArray object.
 *
 * @receiver The string to be parsed as JSON.
 *
 * @return The parsed JSON array represented as a JSONArray object.
 */
val String.jsonArray: JSONArray get() = JSONArray(this)

/**
 * Converts an Iterable of JsonSerializable objects to a JSONArray.
 *
 * @return The JSONArray containing JSON representations of the JsonSerializable objects.
 */
fun Iterable<JsonSerializable>.toJson(): JSONArray = JSONArray().apply {
    for (item in this@toJson)
        put(item.toJson())
}

/**
 * Serialize a JSONArray using a provided JsonSerializer and return a List of the serialized objects.
 *
 * @param serializer The JsonSerializer used to serialize each object in the JSONArray.
 *
 * @return A List of serialized objects.
 */
fun <Result> JSONArray.serialize(serializer: JsonSerializer<Result>): List<Result> = (0 until length()).map { i ->
    val json = getJSONObject(i)
    serializer.fromJson(json)
}

inline fun <T> JSONArray.map(block: JSONArray.(index: Int) -> T) =
    (0 until length()).map { block(it) }
