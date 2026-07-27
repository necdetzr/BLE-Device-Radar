package com.necdetzr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necdetzr.model.ScannedBleDevice

fun LazyListScope.deviceFeed(
    feedUiState: DeviceFeedUiState,
    onDeviceClick: (ScannedBleDevice) -> Unit

){
    when(feedUiState){
        is DeviceFeedUiState.Idle -> Unit
        is DeviceFeedUiState.Scanning -> {
            items(
                items = feedUiState.devices,
                key = { it.macAddress },
                contentType = {"deviceFeedItem"}
            ){
                BleDeviceCard(
                    bleDevice = it,
                    onClick = {onDeviceClick(it)},
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        is DeviceFeedUiState.Success -> {
            items(
                items = feedUiState.devices,
                key = {it.macAddress},
                contentType = {"deviceFeedItem"}
            ){
                BleDeviceCard(
                    bleDevice = it,
                    onClick = {onDeviceClick(it)},
                    modifier = Modifier.padding(8.dp)
                )
            }

        }
    }

}

sealed interface DeviceFeedUiState {
    data object Idle : DeviceFeedUiState
    data class Scanning (val devices:List<ScannedBleDevice>) : DeviceFeedUiState
    data class Success (val devices:List<ScannedBleDevice>) : DeviceFeedUiState
}
