package org.escalaralcoiaicomtat.android.utils.serialization

import org.json.JSONObject

/**
 * A contract for classes that can be serialized to JSON.
 *
 * This interface defines a single method `toJson()` that returns a JSONObject representing the serialized form of the
 * implementing class.
 */
interface JsonSerializable {
    /**
     * Converts the current object to its JSON representation.
     *
     * @return The JSON object representing the current object.
     */
    fun toJson(): JSONObject
}
