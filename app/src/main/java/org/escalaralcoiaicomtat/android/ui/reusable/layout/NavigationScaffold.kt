package org.escalaralcoiaicomtat.android.ui.reusable.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationBar
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationItem
import org.escalaralcoiaicomtat.android.ui.reusable.navigation.NavigationRail

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun NavigationScaffold(
    items: List<NavigationItem?>,
    widthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    pagerState: PagerState = rememberPagerState(initialPage),
    alwaysShowLabel: Boolean = false,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    topBar: @Composable () -> Unit = { },
    snackbarHost: @Composable () -> Unit = { },
    floatingActionButton: @Composable () -> Unit = { },
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    pageContentModifier: Modifier = Modifier,
    pageContent: @Composable ColumnScope.(page: Int) -> Unit
) {
    var currentPage by remember { mutableStateOf(initialPage) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage = it }
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        bottomBar = {
            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                NavigationBar(currentPage, items, alwaysShowLabel) { pagerState.animateScrollToPage(it) }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (widthSizeClass != WindowWidthSizeClass.Compact) {
                NavigationRail(
                    currentPage,
                    items,
                    alwaysShowLabel = alwaysShowLabel,
                    header = header
                ) { pagerState.animateScrollToPage(it) }
            }

            HorizontalPager(
                pageCount = items.size,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                state = pagerState
            ) {
                Column(pageContentModifier) { pageContent(it) }
            }
        }
    }
}
