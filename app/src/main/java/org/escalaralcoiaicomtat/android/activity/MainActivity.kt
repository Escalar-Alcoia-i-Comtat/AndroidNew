package org.escalaralcoiaicomtat.android.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.activity.MainActivity.ICreateOrEdit
import org.escalaralcoiaicomtat.android.activity.creation.EditorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewAreaActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewPathActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewSectorActivity
import org.escalaralcoiaicomtat.android.activity.creation.NewZoneActivity
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.ui.screen.MainScreen
import org.escalaralcoiaicomtat.android.ui.screen.Routes
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.toast
import org.escalaralcoiaicomtat.android.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

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
                onApiKeySubmit = mainViewModel::trySubmittingApiKey,
                onFavoriteToggle = mainViewModel::toggleFavorite,
                onCreateOrEdit = onCreateOrEdit,
                navigate = { navigate(navController, it) }
            )
        }
    }

    private val onSectorView: (sectorId: Long, pathId: Long?) -> Unit = { sectorId, pathId ->
        sectorViewerRequestLauncher.launch(
            SectorViewer.Input(sectorId, pathId)
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

    private fun navigate(navController: NavController?, target: DataEntity?) {
        val currentEntry = navController?.currentBackStackEntry
        val currentEntryArgs = currentEntry?.arguments

        when (target) {
            is Area -> navController?.navigate(
                Routes.NavigationHome.createRoute(areaId = target.id)
            )
            is Zone -> navController?.navigate(
                Routes.NavigationHome.createRoute(
                    areaId = currentEntryArgs?.getString(Routes.Arguments.AREA_ID)?.toLongOrNull(),
                    zoneId = target.id
                )
            )
            is Sector -> onSectorView(target.id, null)
            is Path -> onSectorView(target.parentId, target.id)
            else -> navController?.navigate(
                Routes.NavigationHome.createRoute()
            )
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
