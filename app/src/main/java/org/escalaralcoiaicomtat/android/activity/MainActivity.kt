package org.escalaralcoiaicomtat.android.activity

import android.app.Application
import android.app.assist.AssistContent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.MainActivity.ICreateOrEdit
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewAreaActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewPathActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewSectorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewZoneActivity
import org.escalaralcoiaicomtat.android.network.EndpointUtils
import org.escalaralcoiaicomtat.android.network.ktorHttpClient
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.dao.toggleFavorite
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.screen.MainScreen
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.ui.viewmodel.MainViewModel
import org.escalaralcoiaicomtat.android.utils.toast

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    private val model by viewModels<Model>()

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(onSectorView)
    }

    private val resultCallback = ActivityResultCallback<EditorActivity.Result> { result ->
        when (result) {
            is EditorActivity.Result.CreateSuccess -> toast(R.string.creation_success)
            is EditorActivity.Result.EditSuccess -> toast(R.string.creation_success_edit)
            is EditorActivity.Result.CreateCancelled -> toast(R.string.creation_error_cancelled_toast)
            is EditorActivity.Result.EditCancelled -> toast(R.string.creation_error_edit_cancelled_toast)
            is EditorActivity.Result.Failure -> toast(R.string.creation_error_toast)
            is EditorActivity.Result.Deleted -> toast(R.string.creation_deleted)
        }
    }

    private val newAreaRequestLauncher = registerForActivityResult(
        NewAreaActivity.Contract, resultCallback
    )

    private val newZoneRequestLauncher = registerForActivityResult(
        NewZoneActivity.Contract, resultCallback
    )

    private val newSectorRequestLauncher = registerForActivityResult(
        NewSectorActivity.Contract, resultCallback
    )

    private val newPathRequestLauncher = registerForActivityResult(
        NewPathActivity.Contract, resultCallback
    )

    private val sectorViewerRequestLauncher = registerForActivityResult(
        SectorViewer.Contract
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasShownIntro = Preferences.hasShownIntro(this)
        CoroutineScope(Dispatchers.IO).launch {
            hasShownIntro.collect { shown ->
                if (!shown) {
                    startActivity(
                        Intent(this@MainActivity, IntroActivity::class.java)
                    )
                    finish()
                }
            }
        }

        setContentThemed {
            val windowSizeClass = calculateWindowSizeClass(this)

            val navController = rememberNavController()

            LaunchedEffect(navController) {
                mainViewModel.navController = navController
            }

            MainScreen(
                widthSizeClass = windowSizeClass.widthSizeClass,
                navController = navController,
                onApiKeySubmit = model::trySubmittingApiKey,
                onFavoriteToggle = model::toggleFavorite,
                onCreateOrEdit = onCreateOrEdit,
                onSectorView = onSectorView
            )
        }
    }

    override fun onProvideAssistContent(outContent: AssistContent?) {
        super.onProvideAssistContent(outContent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainViewModel.selection.observe(this) { entity ->
                val uri = (entity as? Area)?.webUrl ?: (entity as? Zone)?.webUrl

                outContent?.webUri = uri
            }
        }
    }

    private val onSectorView: (Sector) -> Unit = {
        sectorViewerRequestLauncher.launch(
            SectorViewer.Input(it.id)
        )
    }

    private val onCreateOrEdit = ICreateOrEdit<ImageEntity> { kClass, parentId, item ->
        when {
            Area::class.isSuperclassOf(kClass) -> {
                newAreaRequestLauncher.launch(item as Area?)
            }

            Zone::class.isSuperclassOf(kClass) -> {
                newZoneRequestLauncher.launch(
                    if (item == null)
                        EditorActivity.Input.fromParent(parentId!!)
                    else
                        EditorActivity.Input.fromElement(parentId!!, item)
                )
            }

            Sector::class.isSuperclassOf(kClass) -> {
                newSectorRequestLauncher.launch(
                    if (item == null)
                        EditorActivity.Input.fromParent(parentId!!)
                    else
                        EditorActivity.Input.fromElement(parentId!!, item)
                )
            }

            Path::class.isSuperclassOf(kClass) -> {
                newPathRequestLauncher.launch(
                    if (item == null)
                        EditorActivity.Input.fromParent(parentId!!)
                    else
                        EditorActivity.Input.fromElement(parentId!!, item)
                )
            }

            else -> throw IllegalArgumentException("Tried to create or edit unsupported type: ${kClass.simpleName}")
        }
    }


    class Model(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val userDao = database.userDao()

        private val apiKeyRegex = Regex("[0-9a-zA-Z]+")

        fun trySubmittingApiKey(apiKey: String): Job = viewModelScope.launch {
            if (apiKeyRegex.matches(apiKey)) {
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
        }

        fun toggleFavorite(data: DataEntity): Job = viewModelScope.launch {
            when (data) {
                is Area -> userDao.toggleFavorite(data)
                is Zone -> userDao.toggleFavorite(data)
                is Sector -> userDao.toggleFavorite(data)
            }
        }
    }

    fun interface ICreateOrEdit<T : ImageEntity> {
        operator fun invoke(
            itemKClass: KClass<*>,
            parentId: Long?,
            item: T?
        )
    }
}
