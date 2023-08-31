package org.escalaralcoiaicomtat.android.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.screen.Routes
import org.escalaralcoiaicomtat.android.ui.screen.Routes.Arguments.AreaId
import org.escalaralcoiaicomtat.android.worker.SyncWorker
import timber.log.Timber
import java.time.Instant
import kotlin.reflect.KClass

class MainViewModel(
    application: Application,
    private val navController: NavHostController,
    private val onSectorView: (Sector) -> Unit
) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.dataDao()

    private val syncWorkers = SyncWorker.getLive(application)
    val isRunningSync = syncWorkers.map { list -> list.any { !it.state.isFinished } }

    private val _selection = MutableLiveData<DataEntity?>()
    val selection: LiveData<DataEntity?> get() = _selection

    private val _currentDestination = MutableLiveData<NavDestination>()

    val creationOptionsList = MutableLiveData<List<DataEntity>>()
    val pendingCreateOperation = MutableLiveData<(DataEntity) -> Unit>()

    val selectionWithCurrentDestination = MediatorLiveData<Pair<DataEntity?, NavDestination?>>().apply {
        addSource(_selection) {
            value = it to _currentDestination.value
        }
        addSource(_currentDestination) {
            value = _selection.value to it
        }
    }

    fun load(areaId: Long?, zoneId: Long?) = viewModelScope.launch(Dispatchers.IO) {
        if (zoneId != null) {
            Timber.d("Loading zone $zoneId...")
            _selection.postValue(dao.getZone(zoneId))
        } else if (areaId != null) {
            Timber.d("Loading area $areaId...")
            _selection.postValue(dao.getArea(areaId))
        } else {
            _selection.postValue(null)
        }
    }

    @Composable
    fun Navigation() {
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                _currentDestination.postValue(destination)
            }

            navController.addOnDestinationChangedListener(listener)

            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
    }

    fun navigate(target: DataEntity?) {
        val currentEntry = navController.currentBackStackEntry
        val currentEntryArgs = currentEntry?.arguments

        when (target) {
            is Area -> navController.navigate(
                Routes.NavigationHome.createRoute(areaId = target.id)
            )
            is Zone -> navController.navigate(
                Routes.NavigationHome.createRoute(
                    areaId = currentEntryArgs?.getString(AreaId)?.toLongOrNull(),
                    zoneId = target.id
                )
            )
            is Sector -> onSectorView(target)
            else -> navController.navigate(
                Routes.NavigationHome.createRoute()
            )
        }
    }

    /**
     * Moves the sector at index [from] to index [to].
     */
    fun moveSector(from: Int, to: Int) = viewModelScope.launch(Dispatchers.IO) {
        val zone = selection.value ?: return@launch Timber.w("Tried to move sector while not in zone")

        Timber.d("Moving sector from $from to $to")
        // Get a list of all the sectors in the zone
        val sectors = dao.getSectorsFromZone(zone.id)?.sectors ?: return@launch
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
    fun <T: DataEntity> createChooser(type: KClass<T>, operation: (T) -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        val list = when(type) {
            Area::class -> dao.getAllAreas()
            Zone::class -> dao.getAllZones()
            Sector::class -> dao.getAllSectors()
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

    companion object {
        fun Factory(
            navController: NavHostController,
            onSectorView: (Sector) -> Unit
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                MainViewModel(application, navController, onSectorView)
            }
        }
    }
}
