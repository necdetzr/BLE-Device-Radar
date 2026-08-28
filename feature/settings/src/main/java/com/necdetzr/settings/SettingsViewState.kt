package com.necdetzr.settings

import com.necdetzr.model.ThemeConfig

private const val DEFAULT_RSSI = -80
private const val DEFAULT_SCAN_PERIOD_MILLIS = 15_000L

data class SettingsViewState(
    val rssi: Int = DEFAULT_RSSI,
    val theme: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val scanPeriod: Long = DEFAULT_SCAN_PERIOD_MILLIS,
    val isLoading: Boolean = true,
)
