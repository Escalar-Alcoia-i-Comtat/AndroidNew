@file:Suppress("TooManyFunctions")

package org.escalaralcoiaicomtat.android.utils

import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializable
import org.escalaralcoiaicomtat.android.utils.serialization.JsonSerializer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant

/** Initializes a new [JSONObject] with `this` string. */
val String.json: JSONObject get() = JSONObject(this)

/**
 * Adds all key-value pairs from the given map to this [JSONObject].
 *
 * @param pairs A map containing the key-value pairs to add to this [JSONObject]. Valid values:
 * 1. [Boolean]
 * 2. [Double]
 * 3. [Integer]
 * 4. [JSONArray]
 * 5. [JSONObject]
 * 6. [Long]
 * 7. [String]
 * 8. [JSONObject.NULL]
 * 9. [JsonSerializable]
 * 10. [Instant]
 *
 * @throws JSONException If the value is non-finite number.
 *
 * @return The updated [JSONObject], after adding the key-value pairs.
 */
fun JSONObject.putAll(pairs: Map<String, Any?>): JSONObject {
    for ((key, value) in pairs) {
        when (value) {
            is JsonSerializable -> put(key, value.toJson())
            is JSONArray -> put(key, value)
            is Instant -> putInstant(key, value)
            is Iterable<*> -> putIterable(key, value)

            else -> put(key, value)
        }
    }
    return this
}

/**
 * Adds all the contents of [iterable] after converting them into a JSON array.
 *
 * @param key The key for the key-value pair in the JSONObject.
 * @param iterable The Iterable of objects to be added as the value.
 */
fun JSONObject.putIterable(key: String, iterable: Iterable<Any?>): JSONObject =
    if (iterable.none())
        put(key, JSONArray())
    else {
        val array = JSONArray()

        for (item in iterable) {
            when (item) {
                is JsonSerializable -> array.put(item.toJson())
                else -> array.put(item)
            }
        }

        put(key, array)
    }

/**
 * Puts the value of the given instant in the passed [key]. Stores as a Long, that matches
 * [Instant.toEpochMilli]. Can be converted back into [Instant] with [Instant.ofEpochMilli].
 *
 * @param key The key to store the instant at.
 * @param value The instant to store.
 *
 * @return The updated [JSONObject]
 */
fun JSONObject.putInstant(key: String, value: Instant): JSONObject =
    put(key, value.toEpochMilli())

/**
 * Constructs a JSON object from a map of key-value pairs.
 *
 * @param pairs a map containing the key-value pairs to be converted to a JSON object
 *
 * @return a new JSON object containing the key-value pairs from the input map
 */
fun jsonOf(pairs: Map<String, Any?>): JSONObject = JSONObject().apply {
    putAll(pairs)
}

/**
 * Creates a JSON object from the given key-value pairs.
 *
 * @param pairs The key-value pairs to be included in the JSON object.
 */
fun jsonOf(vararg pairs: Pair<String, Any?>): JSONObject = jsonOf(pairs.toMap())

/**
 * Retrieves the JSONObject value associated with the given key,
 * or returns null if the key does not exist, or the value is not a JSONObject.
 *
 * @param key the key whose associated value is to be retrieved
 *
 * @return the JSONObject value associated with the given key, or null if the key does not exist or the value is not a
 * JSONObject
 */
fun JSONObject.getJSONObjectOrNull(key: String): JSONObject? =
    try {
        if (has(key)) getJSONObject(key) else null
    } catch (_: JSONException) {
        null
    }

/**
 * Returns the integer value associated with the specified key, or null if the key is not found or the value is not an
 * integer.
 *
 * @param key the key to look up in the JSONObject
 *
 * @return the integer value associated with the key, or null if the key is not found or the value is not an integer
 */
fun JSONObject.getIntOrNull(key: String): Int? =
    try {
        if (has(key)) getInt(key) else null
    } catch (_: JSONException) {
        null
    }

/**
 * Returns the long value associated with the specified key, or null if the key is not found, or the value is not a
 * long.
 *
 * @param key the key to look up in the JSONObject
 *
 * @return the long value associated with the key, or null if the key is not found or the value is not a long
 */
fun JSONObject.getLongOrNull(key: String): Long? =
    try {
        if (has(key)) getLong(key) else null
    } catch (_: JSONException) {
        null
    }

/**
 * Returns the string value associated with the specified key, or null if the key is not found or the value is not a
 * string.
 *
 * @param key the key to look up in the JSONObject
 *
 * @return the string value associated with the key, or null if the key is not found or the value is not a string
 */
fun JSONObject.getStringOrNull(key: String): String? =
    try {
        if (has(key)) getString(key) else null
    } catch (_: JSONException) {
        null
    }

/**
 * Retrieves the unsigned integer value associated with the specified key from this JSONObject.
 *
 * @param key the key whose associated value is to be retrieved as an unsigned integer
 *
 * @throws NumberFormatException If the number at the given key is negative.
 *
 * @return the value associated with the specified key as an unsigned integer
 */
fun JSONObject.getUInt(key: String): UInt {
    val num = getLong(key)
    if (num < 0) throw NumberFormatException("The stored number is not a valid unsigned number ($num). It's negative.")
    return num.toUInt()
}

/**
 * Retrieves the unsigned short value associated with the specified key from this JSONObject.
 *
 * @param key the key whose associated value is to be retrieved as an unsigned short
 *
 * @throws NumberFormatException If the number at the given key is negative, or doesn't fit inside a short.
 *
 * @return the value associated with the specified key as an unsigned short
 */
fun JSONObject.getUShort(key: String): UShort {
    val num = getUInt(key)
    if (num > UShort.MAX_VALUE.toUInt())
        throw NumberFormatException("Present number ($num) is not a valid unsigned short (too large).")
    return num.toUShort()
}

/**
 * Returns the unsigned short value associated with the specified key, or null if the key is not present or the value
 * cannot be parsed as an unsigned short.
 *
 * @param key the key of the value to retrieve
 *
 * @return the unsigned short value associated with the specified key, or null if the key is not present or the value
 * cannot be parsed as an unsigned short
 */
fun JSONObject.getUShortOrNull(key: String): UShort? =
    try {
        if (has(key)) getUShort(key) else null
    } catch (_: JSONException) {
        null
    } catch (_: NumberFormatException) {
        null
    }

/**
 * Retrieves an unsigned integer value from the JSONObject associated with the given key.
 *
 * If the key exists and the value is a valid unsigned integer, it is returned; otherwise null is returned.
 *
 * @param key The key associated with the unsigned integer value
 *
 * @return The unsigned integer value, or null if the key does not exist or the value is not a valid unsigned integer
 */
fun JSONObject.getUIntOrNull(key: String): UInt? =
    try {
        if (has(key)) getUInt(key) else null
    } catch (_: JSONException) {
        null
    } catch (_: NumberFormatException) {
        null
    }

/**
 * Retrieves the value associated with the specified key from the JSON object
 * and converts it to the specified enum type using the enumValueOf function.
 *
 * @param key The key associated with the value to retrieve from the JSON object.
 *
 * @return The enum value associated with the specified key from the JSON object.
 *
 * @throws IllegalArgumentException if the value associated with the key
 * is not a valid enum constant of the specified enum type.
 */
inline fun <reified E: Enum<E>> JSONObject.getEnum(key: String): E = enumValueOf(getString(key))

/**
 * Retrieves the enum value from the JSONObject for the specified key.
 *
 * @param key the key to retrieve the enum value from the JSONObject.
 *
 * @return the enum value corresponding to the key, or null if the key is not present or cannot be parsed as an enum
 * value.
 */
inline fun <reified E : Enum<E>> JSONObject.getEnumOrNull(key: String): E? =
    try {
        if (has(key)) getEnum<E>(key) else null
    } catch (_: JSONException) {
        null
    } catch (_: NumberFormatException) {
        null
    }

/**
 * Returns the boolean value associated with the specified key, or null if there is no such key or the value is not
 * a boolean.
 *
 * @param key the key to look up
 *
 * @return the boolean value associated with the specified key, or null if the key is not found or the value is not
 * a boolean
 */
fun JSONObject.getBooleanOrNull(key: String): Boolean? =
    try {
        if (has(key)) getBoolean(key) else null
    } catch (_: JSONException) {
        null
    }

/**
 * Returns the Instant value associated with the specified key.
 *
 * @param key the key to look up
 *
 * @return the Instant value associated with the specified key.
 */
fun JSONObject.getInstant(key: String): Instant = getLong(key).let(Instant::ofEpochMilli)

inline fun <T, reified S: JsonSerializer<T>> JSONObject.getSerializable(name: String): T {
    val serializer = S::class.objectInstance ?: throw IllegalArgumentException("Could not get serializer instance for ${S::class.simpleName}")
    val json = getJSONObject(name)
    return serializer.fromJson(json)
}
