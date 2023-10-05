package org.escalaralcoiaicomtat.android.activity

import android.app.Activity
import android.app.GrammaticalInflectionManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.GrammaticalInflectionManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.ui.intro.IntroPage
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.launchUrl
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
class IntroActivity : AppCompatActivity() {
    private val viewModel: IntroModel by viewModels()

    private val pages: List<@Composable () -> Unit> get() = listOfNotNull(
        if (Build.VERSION.SDK_INT >= 34) {
            {
                IntroPage(
                    icon = R.drawable.man_raising_hand,
                    title = stringResource(R.string.intro_0_title),
                    message = stringResource(R.string.intro_0_message),
                    options = object : IntroPage.Options<Int>() {
                        override val values: Map<Int, String> = mapOf(
                            Configuration.GRAMMATICAL_GENDER_NEUTRAL to getString(R.string.gender_neuter),
                            Configuration.GRAMMATICAL_GENDER_MASCULINE to getString(R.string.gender_masculine),
                            Configuration.GRAMMATICAL_GENDER_FEMININE to getString(R.string.gender_feminine),
                        )

                        override val defaultIndex: Int = GrammaticalInflectionManagerCompat
                            .getApplicationGrammaticalGender(this@IntroActivity)
                            .let {
                                when (it) {
                                    Configuration.GRAMMATICAL_GENDER_MASCULINE -> 1
                                    Configuration.GRAMMATICAL_GENDER_FEMININE -> 2
                                    else -> 0
                                }
                            }

                        override val label: @Composable () -> String = {
                            stringResource(R.string.choose)
                        }

                        override fun onSelected(key: Int): Boolean {
                            GrammaticalInflectionManagerCompat.setRequestedApplicationGrammaticalGender(
                                this@IntroActivity,
                                key
                            )

                            return true
                        }
                    }
                )
            }
        } else null,
        {
            IntroPage<Any>(
                icon = R.drawable.climbing_color,
                title = stringResource(R.string.intro_1_title),
                message = stringResource(R.string.intro_1_message)
            )
        },
        {
            IntroPage<Any>(
                icon = R.drawable.climbing_helmet_color,
                title = stringResource(R.string.intro_2_title),
                message = stringResource(R.string.intro_2_message)
            )
        },
        {
            IntroPage<Any>(
                icon = R.drawable.belayer_color,
                title = stringResource(R.string.intro_3_title),
                message = stringResource(R.string.intro_3_message),
                action = object : IntroPage.Action() {
                    override val text: @Composable () -> String = {
                        stringResource(R.string.action_view_video)
                    }

                    override fun onClick() {
                        launchUrl("https://www.petzl.com/ES/es/Sport/Video--Asegurar-con-el-GRIGRI?ProductName=GRIGRI")
                    }
                }
            )
        },
        {
            IntroPage<Any>(
                icon = R.drawable.kid_color,
                title = stringResource(R.string.intro_4_title),
                message = stringResource(R.string.intro_4_message)
            )
        },
        {
            IntroPage<Any>(
                icon = R.drawable.drawstring_bag_color,
                title = stringResource(R.string.intro_5_title),
                message = stringResource(R.string.intro_5_message)
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when(GrammaticalInflectionManagerCompat.getApplicationGrammaticalGender(this)) {
            Configuration.GRAMMATICAL_GENDER_FEMININE -> Timber.i("User's gender is Feminine")
            Configuration.GRAMMATICAL_GENDER_MASCULINE -> Timber.i("User's gender is Masculine")
            Configuration.GRAMMATICAL_GENDER_NEUTRAL -> Timber.i("User's gender is Neutral")
            Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED -> Timber.i("User's gender is not specified")
            else -> Timber.w("User's gender is unknown")
        }

        setContentThemed {
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current

            val scope = rememberCoroutineScope()

            var pages by remember { mutableStateOf(pages) }
            if (Build.VERSION.SDK_INT >= 34) {
                val grammaticalGender by viewModel.grammaticalGender.observeAsState()
                LaunchedEffect(grammaticalGender) {
                    pages = this@IntroActivity.pages
                }
            }

            val pagerState = rememberPagerState { pages.size }
            var currentPage by remember { mutableIntStateOf(0) }

            var scrollProgress by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }
                    .collect { currentPage = it }
            }

            val screenWidth = configuration.screenWidthDp.dp
            val widthPx = with(density) { screenWidth.toPx() }

            PredictiveBackHandler { progress: Flow<BackEventCompat> ->
                // code for gesture back started
                try {
                    progress.collect {
                        val diff = widthPx * (scrollProgress - it.progress)
                        scrollProgress = it.progress
                        pagerState.scrollBy(diff)
                    }
                    // code for completion
                    if (currentPage == 0) {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage - 1)
                        }
                    }
                } catch (e: CancellationException) {
                    // code for cancellation
                } finally {
                    pagerState.scrollBy(-widthPx * scrollProgress)
                    scrollProgress = 0f
                }
            }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (currentPage + 1 >= pages.size) {
                                viewModel.finishAndLaunchMain(this@IntroActivity)
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentPage + 1)
                                }
                            }
                        }
                    ) {
                        if (currentPage + 1 < pages.size)
                            Icon(Icons.Rounded.ChevronRight, stringResource(R.string.action_next))
                        else
                            Icon(Icons.Rounded.Check, stringResource(R.string.action_done))
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TextButton(
                        onClick = { viewModel.finishAndLaunchMain(this@IntroActivity) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .zIndex(1f)
                    ) {
                        Text(stringResource(R.string.action_skip))
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        pages[page].invoke()
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (Build.VERSION.SDK_INT >= 34) {
            viewModel.setGrammaticalGender(newConfig.grammaticalGender)
        }
    }

    class IntroModel : ViewModel() {
        @get:RequiresApi(34)
        @delegate:RequiresApi(34)
        val grammaticalGender: MutableLiveData<Int> by lazy {
            MutableLiveData(
                Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED
            )
        }

        /**
         * Stores in preferences that the intro has already been shown, and launches MainActivity.
         */
        fun finishAndLaunchMain(activity: Activity) {
            viewModelScope.launch(Dispatchers.IO) {
                Preferences.markIntroShown(activity)

                withContext(Dispatchers.Main) {
                    activity.startActivity(
                        Intent(activity, MainActivity::class.java)
                    )
                }
            }
        }

        /**
         * Updates [grammaticalGender] with the given one.
         * @param gender The new gender to set. UI will update. Must be one of:
         * - [Configuration.GRAMMATICAL_GENDER_NEUTRAL]
         * - [Configuration.GRAMMATICAL_GENDER_MASCULINE]
         * - [Configuration.GRAMMATICAL_GENDER_FEMININE]
         * - [Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED]
         */
        @RequiresApi(34)
        fun setGrammaticalGender(gender: Int) {
            grammaticalGender.postValue(gender)
        }
    }
}
