package com.necdetzr.bledeviceradar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.necdetzr.bledeviceradar.navigation.TOP_LEVEL_NAV_ITEMS
import com.necdetzr.navigation.NavigationState
import com.necdetzr.navigation.rememberNavigationState
import com.necdetzr.radar.api.RadarNavKey


@Composable
fun rememberBleAppState(
) : BleAppState{
    val navigationState = rememberNavigationState(RadarNavKey, TOP_LEVEL_NAV_ITEMS.keys)
    return remember(navigationState){
        BleAppState(
            navigationState
        )
    }
}


@Stable
class BleAppState(
    val navigationState: NavigationState
)
