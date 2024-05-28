package org.escalaralcoiaicomtat.android.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.exception.remote.RequestException
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.bodyAsJson
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.dao.toggleFavorite
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Sector
import timber.log.Timber

class SectorViewerModel(
    application: Application,
    private val sectorId: Long
) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dataDao = database.dataDao()
    private val userDao = database.userDao()

    var sector by mutableStateOf<Sector?>(null)
        private set

    val isFavorite = userDao.getSectorFlow(sectorId)

    private val pathWithBlocks = dataDao.getPathWithBlocksFlow(sectorId)

    val paths = pathWithBlocks.map { list -> list.map { it.path } }

    val blocks = pathWithBlocks.map { list ->
        list.associate { it.path to it.blocks }
    }

    var gpxProgress by mutableStateOf<Pair<Long, Long>?>(null)
        private set

    var selection by mutableStateOf<Selection?>(null)
        private set

    val apiKey = Preferences.getApiKey(application).asLiveData(Dispatchers.Main)

    val hasValidId = sectorId >= 0

    fun load() {
        if (!hasValidId) return

        viewModelScope.launch(Dispatchers.IO) {
            sector = dataDao.getSector(sectorId) ?: error("Could not find sector with id $sectorId")
        }
    }

    fun select(selection: Selection) { this.selection = selection }

    fun clearSelection() { selection = null }

    fun createBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
        ktorHttpClient.post(EndpointUtils.getUrl("block/${blocking.parentId}")) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            setBody(blocking.toJson().toString())
        }.apply {
            if (status == HttpStatusCode.Created) {
                // Update successful
                Timber.d("Created block successfully.")

                val element = bodyAsJson()
                    .getJSONObject("data")
                    .getJSONObject("element")
                    .let(Blocking::fromJson)
                dataDao.insert(element)
            } else {
                Timber.e("Could not create block in server.")
                throw RequestException(status, bodyAsJson())
            }
        }
    }

    fun updateBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
        dataDao.update(blocking)
    }

    fun deleteBlock(blocking: Blocking) = viewModelScope.launch(Dispatchers.IO) {
        dataDao.notifyDeletion(
            LocalDeletion(type = "block", deleteId = blocking.id)
        )
        dataDao.delete(blocking)
    }

    /**
     * Toggles the favorite status of the currently loaded sector. Does nothing if no sector
     * is loaded.
     */
    fun toggleSectorFavorite() {
        val sector = sector ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userDao.toggleFavorite(sector)
        }
    }

    fun downloadGpx() {
        // Allow only one concurrent download
        gpxProgress ?: return
        // Make sure there's a sector
        val sector = sector ?: return

        viewModelScope.launch(Dispatchers.IO) {
            sector.updateGpxIfNeeded(getApplication()) { current, max ->
                gpxProgress = (current to max)
            }
            gpxProgress = null
        }
    }


    sealed class Selection {
        data class Index(val index: Int) : Selection()

        data object SectorInformation : Selection()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val sectorId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

            return SectorViewerModel(application, sectorId) as T
        }
    }
}
