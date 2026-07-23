package com.necdetzr.settings

import com.necdetzr.model.ThemeConfig

data class SettingsViewState (
    val rssi: Int = -80,
    val theme: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val scanPeriod:Long = 15_000L,
    val isLoading: Boolean = true
)
