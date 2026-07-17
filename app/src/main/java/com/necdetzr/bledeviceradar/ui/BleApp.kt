package com.necdetzr.bledeviceradar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.necdetzr.bledeviceradar.navigation.TOP_LEVEL_NAV_ITEMS
import com.necdetzr.designsystem.component.BleBackground
import com.necdetzr.designsystem.component.BleNavigationSuiteScaffold
import com.necdetzr.designsystem.component.TopAppBar
import com.necdetzr.history.navigation.historyEntry
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
){
    val navigator = remember{ Navigator(appState.navigationState) }
    BleBackground {
        BleNavigationSuiteScaffold(
            navigationSuiteItems ={
                TOP_LEVEL_NAV_ITEMS.forEach { (navKey,navItem)->
                    val selected = navKey == appState.navigationState.currentTopKey
                    item(
                        selected = selected,
                        onClick = {navigator.navigate(navKey)},
                        icon = {
                            Icon(
                                imageVector = navItem.unSelectedIcon,
                                contentDescription = null
                            )
                        },
                        selectedIcon = {
                            Icon(
                                imageVector = navItem.selectedIcon,
                                contentDescription = null
                            )
                        },
                        label ={
                            Text(
                                text = stringResource(navItem.iconTextId)
                            )
                        },

                        )
                }
            },
            windowAdaptiveInfo = windowAdaptiveInfo
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal
                            )
                        )
                ) {
                    var shouldShowTopAppBar = false
                    if(appState.navigationState.currentKey in appState.navigationState.topLevelKeys){
                        shouldShowTopAppBar = true

                        val destination = TOP_LEVEL_NAV_ITEMS[appState.navigationState.currentTopKey]
                            ?: error("Top level nav item not found for ${appState.navigationState.currentTopKey}")
                        TopAppBar(
                            title = "Ble Radar Good Radar"
                        )
                    }
                    Box(
                        modifier = Modifier.consumeWindowInsets(
                            if(shouldShowTopAppBar){
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)

                            }else{
                                WindowInsets(0, 0, 0, 0)

                            }
                        )
                    ){
                        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
                        val entryProvider = entryProvider {
                            radarEntry(navigator)
                            historyEntry(navigator)
                            settingsEntry(navigator)
                        }
                        NavDisplay(
                            entries = appState.navigationState.toEntries(entryProvider),
                            sceneStrategy = listDetailStrategy,
                            onBack = {navigator.goBack()}
                        )
                    }
                }
            }

        }
    }


}
