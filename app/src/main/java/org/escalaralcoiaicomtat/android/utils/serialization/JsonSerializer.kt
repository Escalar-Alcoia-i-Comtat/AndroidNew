package org.escalaralcoiaicomtat.android.utils.serialization

import org.json.JSONObject

/**
 * Represents a JSON serializer interface.
 *
 * This interface provides a contract for classes that can convert a JSONObject into a specific result type.
 * Implementations of this interface should define the logic for deserializing a JSON object to the desired result type.
 *
 * @param Result the result type that the JSON object will be deserialized to
 */
interface JsonSerializer <Result> {
    /**
     * Converts a JSON object to a Result object.
     *
     * @param json The JSON object to convert.
     *
     * @return The converted Result object.
     */
    fun fromJson(json: JSONObject): Result
}
