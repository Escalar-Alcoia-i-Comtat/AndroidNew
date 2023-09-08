package org.escalaralcoiaicomtat.android.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.storage.Preferences
import org.escalaralcoiaicomtat.android.ui.intro.IntroPage
import org.escalaralcoiaicomtat.android.ui.logic.BackInvokeHandler
import org.escalaralcoiaicomtat.android.ui.theme.setContentThemed
import org.escalaralcoiaicomtat.android.utils.launchUrl

@OptIn(ExperimentalFoundationApi::class)
class IntroActivity : AppCompatActivity() {
    private val viewModel: IntroModel by viewModels()

    private val pages: List<@Composable () -> Unit> = listOf(
        {
            IntroPage(
                icon = R.drawable.climbing_color,
                title = stringResource(R.string.intro_1_title),
                message = stringResource(R.string.intro_1_message)
            )
        },
        {
            IntroPage(
                icon = R.drawable.climbing_helmet_color,
                title = stringResource(R.string.intro_2_title),
                message = stringResource(R.string.intro_2_message)
            )
        },
        {
            IntroPage(
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
            IntroPage(
                icon = R.drawable.kid_color,
                title = stringResource(R.string.intro_4_title),
                message = stringResource(R.string.intro_4_message)
            )
        },
        {
            IntroPage(
                icon = R.drawable.drawstring_bag_color,
                title = stringResource(R.string.intro_5_title),
                message = stringResource(R.string.intro_5_message)
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentThemed {
            val scope = rememberCoroutineScope()

            val pagerState = rememberPagerState { pages.size }
            var currentPage by remember { mutableIntStateOf(0) }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }
                    .collect { currentPage = it }
            }

            BackInvokeHandler {
                if (currentPage == 0) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(currentPage - 1)
                    }
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

    class IntroModel : ViewModel() {
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
    }
}
