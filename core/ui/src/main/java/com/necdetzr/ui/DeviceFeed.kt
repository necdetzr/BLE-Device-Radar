package com.necdetzr.ui

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import com.necdetzr.model.BleDevice

fun LazyListScope.deviceFeed(
    feedUiState: DeviceFeedUiState,
    onDeviceClick: (BleDevice) -> Unit

){
    when(feedUiState){
        is DeviceFeedUiState.Idle -> Unit
        is DeviceFeedUiState.Scanning -> {
            items(
                items = feedUiState.devices,
                key = {it.macAddress},
                contentType = {"deviceFeedItem"}
            ){
                BleDeviceCard(
                    bleDevice = it,
                    onClick = {}
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
                    onClick = {onDeviceClick(it)}
                )
            }

        }
    }

}

sealed interface DeviceFeedUiState {
    data object Idle : DeviceFeedUiState
    data class Scanning (val devices:List<BleDevice>) : DeviceFeedUiState
    data class Success (val devices:List<BleDevice>) : DeviceFeedUiState
}
