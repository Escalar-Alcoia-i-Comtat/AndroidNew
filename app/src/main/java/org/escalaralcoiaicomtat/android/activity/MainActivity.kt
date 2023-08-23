package org.escalaralcoiaicomtat.android.activity

import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.activity.creation.CreatorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewAreaActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewSectorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewZoneActivity
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.screen.MainScreen
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    private val model by viewModels<Model>()

    private val newAreaRequestLauncher = registerForActivityResult(
        NewAreaActivity.Contract
    ) { }

    private val newZoneRequestLauncher = registerForActivityResult(
        NewZoneActivity.Contract
    ) { }

    private val newSectorRequestLauncher = registerForActivityResult(
        NewSectorActivity.Contract
    ) { }

    private val sectorViewerRequestLauncher = registerForActivityResult(
        SectorViewer.Contract
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentThemed {
            val windowSizeClass = calculateWindowSizeClass(this)

            val navController = rememberNavController()

            MainScreen(
                widthSizeClass = windowSizeClass.widthSizeClass,
                navController = navController,
                onApiKeySubmit = model::trySubmittingApiKey,
                onFavoriteToggle = model::toggleFavorite,
                onCreateArea = { newAreaRequestLauncher.launch(null) },
                onCreateZone = {
                    newZoneRequestLauncher.launch(
                        CreatorActivity.Input(it)
                    )
                },
                onCreateSector = {
                    newSectorRequestLauncher.launch(
                        CreatorActivity.Input(it)
                    )
                },
                onCreatePath = {
                    /* TODO */
                },
                onSectorView = {
                    sectorViewerRequestLauncher.launch(
                        SectorViewer.Input(it.id)
                    )
                }
            )
        }
    }


    class Model(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val dao = database.dataDao()

        fun trySubmittingApiKey(apiKey: String): Job = viewModelScope.launch {
            ktorHttpClient.submitFormWithBinaryData(
                url = EndpointUtils.getUrl("area"),
                formData = formData()
            ) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }.apply {
                if (status == HttpStatusCode.BadRequest) {
                    // Request was "successful"
                    Preferences.setApiKey(getApplication(), apiKey)
                } else {
                    // Request failed
                }
            }
        }

        fun toggleFavorite(data: DataEntity): Job = viewModelScope.launch {
            when (data) {
                is Area -> dao.update(data.copy(isFavorite = !data.isFavorite))
                is Zone -> dao.update(data.copy(isFavorite = !data.isFavorite))
                is Sector -> dao.update(data.copy(isFavorite = !data.isFavorite))
            }
        }
    }
}
