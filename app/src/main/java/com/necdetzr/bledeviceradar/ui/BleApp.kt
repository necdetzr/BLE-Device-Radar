package com.necdetzr.bledeviceradar.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.necdetzr.bledeviceradar.R
import com.necdetzr.bledeviceradar.navigation.TOP_LEVEL_NAV_ITEMS
import com.necdetzr.designsystem.component.BleBackground
import com.necdetzr.designsystem.component.BleNavigationSuiteScaffold
import com.necdetzr.designsystem.component.BleNavigationSuiteScope
import com.necdetzr.designsystem.component.TopAppBar
import com.necdetzr.history.navigation.historyEntry
import com.necdetzr.history.navigation.historySearch
import com.necdetzr.navigation.Navigator
import com.necdetzr.navigation.toEntries
import com.necdetzr.radar.navigation.radarEntry
import com.necdetzr.settings.navigation.settingsEntry

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun BleApp(
    appState: BleAppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
) {
    val navigator = remember { Navigator(appState.navigationState) }

    BleBackground(modifier = modifier) {
        BleNavigationSuiteScaffold(
            navigationSuiteItems = {
                bleNavigationItems(scope = this, appState = appState, navigator =  navigator)
            },
            windowAdaptiveInfo = windowAdaptiveInfo
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { paddingValues ->
                BleAppContent(
                    appState = appState,
                    navigator = navigator,
                    paddingValues = paddingValues
                )
            }
        }
    }
}
private fun bleNavigationItems(
    scope: BleNavigationSuiteScope,
    appState: BleAppState,
    navigator: Navigator
) {
    TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
        val selected = navKey == appState.navigationState.currentTopKey
        scope.item(
            selected = selected,
            onClick = { navigator.navigate(navKey) },
            icon = {
                Icon(
                    imageVector = if (selected) navItem.selectedIcon else navItem.unSelectedIcon,
                    contentDescription = null
                )
            },

            label = {
                Text(text = stringResource(navItem.iconTextId))
            }
        )
    }
}
@SuppressLint("VisibleForTests")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun BleAppContent(
    appState: BleAppState,
    navigator: Navigator,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val isTopLevel = appState.navigationState.currentKey in appState.navigationState.topLevelKeys

    Column(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
    ) {
        if (isTopLevel) {
            TopAppBar(
                title = stringResource(R.string.app_name)
            )
        }

        Box(
            modifier = Modifier.consumeWindowInsets(
                if (isTopLevel) {
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                } else {
                    WindowInsets(0, 0, 0, 0)
                }
            )
        ) {
            val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
            val entryProvider = entryProvider {
                radarEntry(navigator)
                historyEntry(navigator)
                settingsEntry(navigator)
                historySearch(navigator)
            }

            NavDisplay(
                entries = appState.navigationState.toEntries(entryProvider),
                sceneStrategy = listDetailStrategy,
                onBack = { navigator.goBack() }
            )
        }
    }
}
