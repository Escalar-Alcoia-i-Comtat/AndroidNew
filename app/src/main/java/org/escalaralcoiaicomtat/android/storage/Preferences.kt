package org.escalaralcoiaicomtat.android.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

val apiKeyPreference = stringPreferencesKey("api_key")

val everSynchronized = booleanPreferencesKey("every_sync")

val lastSync = longPreferencesKey("last_sync")

/**
 * The server's last update time of the last fetch.
 */
val lastUpdate = longPreferencesKey("last_update")

/**
 * The last local modification date.
 */
val lastModification = longPreferencesKey("last_modification")

val shownIntro = booleanPreferencesKey("shown_intro")

/**
 * Whether the user has opted in to error collection.
 */
val errorCollection = booleanPreferencesKey("error_collection")

/**
 * Whether the user has opted in to performance metrics collection.
 */
val performanceMetrics = booleanPreferencesKey("performance_metrics")

object Preferences {
    fun getApiKey(context: Context) = context.dataStore
        .data
        .map { it[apiKeyPreference] }

    fun hasEverSynchronized(context: Context) = context.dataStore
        .data
        .map { it[everSynchronized] ?: false }

    fun getLastSync(context: Context) = context.dataStore
        .data
        .map { it[lastSync]?.let(Instant::ofEpochMilli) }

    fun getLastUpdate(context: Context): Flow<Instant?> = context.dataStore
        .data
        .map { it[lastSync]?.let(Instant::ofEpochMilli) }

    fun getLastModification(context: Context): Flow<Instant?> = context.dataStore
        .data
        .map { it[lastModification]?.let(Instant::ofEpochMilli) }

    fun hasShownIntro(context: Context) = context.dataStore
        .data
        .map { it[shownIntro] ?: false }

    fun hasOptedInForErrorCollection(context: Context) = context.dataStore
        .data
        .map { it[errorCollection] ?: true }

    fun hasOptedInForPerformanceMetrics(context: Context) = context.dataStore
        .data
        .map { it[performanceMetrics] ?: true }


    suspend fun setApiKey(context: Context, value: String) =
        context.dataStore.edit { it[apiKeyPreference] = value }

    suspend fun markAsSynchronized(context: Context) = context.dataStore
        .edit { it[everSynchronized] = true }

    suspend fun setLastSync(context: Context, value: Instant) =
        context.dataStore.edit { it[lastSync] = value.toEpochMilli() }

    suspend fun setLastUpdate(context: Context, value: Instant?) =
        context.dataStore.edit {
            if (value != null)
                it[lastUpdate] = value.toEpochMilli()
            else
                it.remove(lastUpdate)
        }

    suspend fun setLastModification(context: Context, value: Instant?) =
        context.dataStore.edit {
            if (value != null)
                it[lastModification] = value.toEpochMilli()
            else
                it.remove(lastModification)
        }

    suspend fun markIntroShown(context: Context) = context.dataStore
        .edit { it[shownIntro] = true }

    suspend fun optInForErrorCollection(context: Context, value: Boolean) =
        context.dataStore.edit { it[errorCollection] = value }

    suspend fun optInForPerformanceMetrics(context: Context, value: Boolean) =
        context.dataStore.edit { it[performanceMetrics] = value }
}
