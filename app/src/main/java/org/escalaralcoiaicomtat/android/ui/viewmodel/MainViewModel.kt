package org.escalaralcoiaicomtat.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber
import java.time.Instant

class MainViewModel(
    application: Application,
    private val onSectorView: (Sector) -> Unit
) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.dataDao()

    private val syncWorkers = SyncWorker.getLive(application)
    val isRunningSync = syncWorkers.map { list -> list.any { !it.state.isFinished } }

    private val _currentArea = MutableLiveData<Area?>()
    val currentArea: LiveData<Area?> get() = _currentArea

    private val _currentZone = MutableLiveData<Zone?>()
    val currentZone: LiveData<Zone?> get() = _currentZone

    val currentSelection = MediatorLiveData<DataEntity?>().apply {
        addSource(currentArea) {
            if (value is Area && it == null)
                value = null
            else if (it != null)
                value = it
        }
        addSource(currentZone) {
            if (value is Zone && it == null)
                value = currentArea.value
            else if (it != null)
                value = it
        }
    }

    /**
     * Sets all currents to null.
     */
    fun clear() {
        _currentZone.postValue(null)
        _currentArea.postValue(null)
    }

    fun <E: DataEntity> navigateTo(entity: E?) = viewModelScope.launch(Dispatchers.IO) {
        if (entity == null) return@launch clear()
        when (entity) {
            is Area -> {
                _currentZone.postValue(null)
                _currentArea.postValue(entity)
            }

            is Zone -> {
                _currentZone.postValue(entity)
                if (_currentArea.value?.id != entity.areaId) {
                    // If the change modifies areas, search for the correct one, and replace currentArea
                    val area = dao.getArea(entity.areaId)
                    _currentArea.postValue(area)
                }
            }

            is Sector -> onSectorView(entity)

            else -> {
                throw IllegalArgumentException("Cannot navigate to unknown type ${entity::class.simpleName}")
            }
        }
    }

    /**
     * Moves the sector at index [from] to index [to].
     */
    fun moveSector(from: Int, to: Int) = viewModelScope.launch(Dispatchers.IO) {
        Timber.d("Moving sector from $from to $to")
        // Only move if currentZone is not null
        val currentZone = currentZone.value ?: return@launch
        // Get a list of all the sectors in the zone
        val sectors = dao.getSectorsFromZone(currentZone.id)?.sectors ?: return@launch
        // Sort the sectors by current weight, and make sure the list is mutable
        val sortedSectors = sectors.sortedBy { it.weight }.toMutableList()
        // Move the desired item
        sortedSectors.apply { add(to, removeAt(from)) }
        // Update weights
        val updatedSectors = sortedSectors
            .mapIndexed { index, sector ->
                sector.copy(
                    timestamp = Instant.now(),
                    weight = index.toString(36).padStart(4, '0')
                )
            }
        // Update the database entries
        dao.update(*updatedSectors.toTypedArray())
    }

    companion object {
        fun Factory(
            onSectorView: (Sector) -> Unit
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainViewModel(application, onSectorView)
            }
        }
    }
}
