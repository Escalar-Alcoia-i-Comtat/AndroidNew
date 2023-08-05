package org.escalaralcoiaicomtat.android.utils

/**
 * Checks if all the given items are null.
 *
 * @param items The items to check for null.
 *
 * @return True if all items are null, false otherwise. If [items] is empty, always returns true.
 */
fun areAllNull(vararg items: Any?): Boolean = items.all { it == null }

/**
 * Checks if any of the given items is null.
 *
 * @param items The items to be checked.
 * 
 * @return true if any of the items is null, false otherwise. If [items] is empty, always returns false.
 */
fun isAnyNull(vararg items: Any?): Boolean = items.any { it == null }
