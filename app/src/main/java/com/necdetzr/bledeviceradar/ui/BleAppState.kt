package com.necdetzr.bledeviceradar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.necdetzr.bledeviceradar.navigation.TOP_LEVEL_NAV_ITEMS
import com.necdetzr.navigation.NavigationState
import com.necdetzr.navigation.rememberNavigationState
import com.necdetzr.radar.api.RadarNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


@Composable
fun rememberBleAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) : BleAppState{
    val navigationState = rememberNavigationState(RadarNavKey, TOP_LEVEL_NAV_ITEMS.keys)
    return remember(
        coroutineScope,
        navigationState
    ){
        BleAppState(
            coroutineScope,
            navigationState

        )
    }
}



@Stable
class BleAppState(
    coroutineScope: CoroutineScope,
    val navigationState: NavigationState
){
    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    fun updateBluetoothState(isEnabled:Boolean){
        _isBluetoothEnabled.value = isEnabled
    }
}
