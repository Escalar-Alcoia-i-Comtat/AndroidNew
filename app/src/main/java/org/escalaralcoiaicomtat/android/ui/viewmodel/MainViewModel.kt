package org.escalaralcoiaicomtat.android.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import java.time.Instant
import kotlin.reflect.KClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.dao.search
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dataDao = database.dataDao()
    private val userDao = database.userDao()

    var navController: NavHostController? = null

    private val syncWorkers = SyncWorker.getFlow(application)
    val isRunningSync = syncWorkers.map { list -> list.any { !it.state.isFinished } }

    private val _selection = MutableStateFlow<ImageEntity?>(null)
    val selection: StateFlow<ImageEntity?> get() = _selection.asStateFlow()

    private val _currentDestination = MutableStateFlow<NavDestination?>(null)

    val creationOptionsList = MutableLiveData<List<DataEntity>?>()
    val pendingCreateOperation = MutableLiveData<((DataEntity) -> Unit)?>()

    val areas = dataDao.getAllAreasFlow()
    val zones = dataDao.getAllZonesFlow()
    val sectors = dataDao.getAllSectorsFlow()
    val paths = dataDao.getAllPathsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val areaWithZones = selection.flatMapLatest {
        if (it is Area) {
            dataDao.getZonesFromAreaFlow(it.id)
        } else {
            emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val sectorsFromZone = selection.flatMapLatest {
        if (it is Zone) {
            dataDao.getSectorsFromZoneFlow(it.id)
        } else {
            emptyFlow()
        }
    }

    private val favoriteAreas = userDao.getAllAreasFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val favoriteZones = userDao.getAllZonesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val favoriteSectors = userDao.getAllSectorsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _favorites = MutableLiveData<List<ImageEntity>>()
    val favorites: LiveData<List<ImageEntity>> get() = _favorites

    private val favoritesCollector = FlowCollector<List<*>?> {
        updateFavorites()
    }

    val selectionWithCurrentDestination: Flow<Pair<DataEntity?, NavDestination?>> =
        combine(selection, _currentDestination) { values ->
            (values[0] as DataEntity?) to (values[1] as NavDestination?)
        }

    val searchQuery = MutableStateFlow("")
    val isSearching = MutableStateFlow(false)
    val loadingSearchResults = MutableStateFlow(false)

    val searchResults = MutableStateFlow(emptyList<DataEntity>())

    init {
        viewModelScope.launch { favoriteAreas.collect(favoritesCollector) }
        viewModelScope.launch { favoriteZones.collect(favoritesCollector) }
        viewModelScope.launch { favoriteSectors.collect(favoritesCollector) }
    }

    fun load(areaId: Long?, zoneId: Long?) = viewModelScope.launch(Dispatchers.IO) {
        if (zoneId != null) {
            Timber.d("Loading zone $zoneId...")
            _selection.tryEmit(dataDao.getZone(zoneId))
        } else if (areaId != null) {
            Timber.d("Loading area $areaId...")
            _selection.tryEmit(dataDao.getArea(areaId))
        } else {
            _selection.tryEmit(null)
        }
    }

    @Composable
    fun Navigation() {
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                _currentDestination.tryEmit(destination)
            }

            navController?.addOnDestinationChangedListener(listener)

            onDispose {
                navController?.removeOnDestinationChangedListener(listener)
            }
        }
    }

    /**
     * Moves the sector at index [from] to index [to].
     */
    fun moveSector(from: Int, to: Int) = viewModelScope.launch(Dispatchers.IO) {
        val zone =
            selection.value ?: return@launch Timber.w("Tried to move sector while not in zone")

        Timber.d("Moving sector from $from to $to")
        // Get a list of all the sectors in the zone
        val sectors = dataDao.getSectorsFromZone(zone.id)?.sectors ?: return@launch
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
        for (sector in updatedSectors) dataDao.update(sector)
    }

    /**
     * Updates [creationOptionsList] with the desired options to select the parent to create a new
     * child from.
     *
     * Once loaded, will set the value of [creationOptionsList] to a list of all the elements
     * stored in the database with type [type].
     *
     * @param type The parent element type of the new element.
     *
     * @throws IllegalArgumentException When [type] is not valid.
     */
    fun <T : DataEntity> createChooser(type: KClass<T>, operation: (T) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            val list = when (type) {
                Area::class -> dataDao.getAllAreas()
                Zone::class -> dataDao.getAllZones()
                Sector::class -> dataDao.getAllSectors()
                else -> throw IllegalArgumentException("Could not select as parent ${type.simpleName}")
            }
            pendingCreateOperation.postValue {
                @Suppress("UNCHECKED_CAST")
                operation(it as T)
            }
            creationOptionsList.postValue(list)
        }

    fun dismissChooser() {
        pendingCreateOperation.postValue(null)
        creationOptionsList.postValue(null)
    }

    private fun updateFavorites() = viewModelScope.launch(Dispatchers.IO) {
        val areas = favoriteAreas.value ?: emptyList()
        val zones = favoriteZones.value ?: emptyList()
        val sectors = favoriteSectors.value ?: emptyList()

        val favorites = mutableListOf<ImageEntity>()

        for (favorite in areas) {
            dataDao.getArea(favorite.areaId)?.let(favorites::add)
        }
        for (favorite in zones) {
            dataDao.getZone(favorite.zoneId)?.let(favorites::add)
        }
        for (favorite in sectors) {
            dataDao.getSector(favorite.sectorId)?.let(favorites::add)
        }

        _favorites.postValue(favorites)
    }

    fun search(query: String) = viewModelScope.launch {
        try {
            loadingSearchResults.tryEmit(true)

            Timber.d("Searching for $query")

            val results = dataDao.search(query)

            searchResults.tryEmit(
                results.also { Timber.i("Got ${it.size} results.") }
            )
        } finally {
            loadingSearchResults.tryEmit(false)
        }
    }
}
