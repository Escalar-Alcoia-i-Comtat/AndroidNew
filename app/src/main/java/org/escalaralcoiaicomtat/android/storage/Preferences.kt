package org.escalaralcoiaicomtat.android.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

val apiKeyPreference = stringPreferencesKey("api_key")
val everSynchronized = booleanPreferencesKey("every_sync")

object Preferences {
    fun getApiKey(context: Context) = context.dataStore
        .data
        .map { it[apiKeyPreference] }

    fun hasEverSynchronized(context: Context) = context.dataStore
        .data
        .map { it[everSynchronized] ?: false }

    suspend fun setApiKey(context: Context, value: String) =
        context.dataStore.edit { it[apiKeyPreference] = value }

    suspend fun markAsSynchronized(context: Context) = context.dataStore
        .edit { it[everSynchronized] = true }
}
