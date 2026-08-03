package com.necdetzr.radar

import com.necdetzr.ui.DeviceFeedUiState

data class RadarScreenState(
    val feedState: DeviceFeedUiState = DeviceFeedUiState.Idle,
    val radarMessage: RadarUserMessage? = null,
    val showAlertDialog: Boolean = false,
    val saveButtonEnabled: Boolean = true

)
