package org.escalaralcoiaicomtat.android.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
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

class MainViewModel(
    application: Application,
    private val onSectorView: (Sector) -> Unit
) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.dataDao()

    private val syncWorkers = SyncWorker.getLive(application)
    val isRunningSync = syncWorkers.map { list -> list.any { !it.state.isFinished } }

    @Volatile
    private var navController: NavHostController? = null

    private val _selection = MutableLiveData<DataEntity?>()
    val selection: LiveData<DataEntity?> get() = _selection

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
    fun Navigation(navController: NavHostController) {
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                val backStack = controller.currentBackStack.value
                backStack.forEach { entry ->
                    Timber.i("Route: ${entry.destination.route}")
                }
            }

            this@MainViewModel.navController = navController

            navController.addOnDestinationChangedListener(listener)

            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
    }

    fun navigate(target: DataEntity?) {
        val navController = navController ?: return
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
