package com.necdetzr.bledeviceradar.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.necdetzr.designsystem.icons.BleIcons
import com.necdetzr.history.api.HistoryNavKey
import com.necdetzr.radar.R
import com.necdetzr.radar.api.RadarNavKey
import com.necdetzr.settings.api.SettingsNavKey

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @StringRes val iconTextId:Int,
    @StringRes val titleTextId:Int
)

val RADAR = TopLevelNavItem(
    selectedIcon = BleIcons.Radar,
    unSelectedIcon = BleIcons.Radar,
    iconTextId = R.string.feature_radar_title,
    titleTextId = R.string.feature_radar_title
)

val HISTORY = TopLevelNavItem(
    selectedIcon = BleIcons.History,
    unSelectedIcon = BleIcons.History,
    iconTextId = com.necdetzr.history.R.string.feature_history_title,
    titleTextId = com.necdetzr.history.R.string.feature_history_title

)

val SETTINGS = TopLevelNavItem(
    selectedIcon = BleIcons.Settings,
    unSelectedIcon = BleIcons.Settings,
    iconTextId = com.necdetzr.settings.R.string.feature_settings_title,
    titleTextId = com.necdetzr.settings.R.string.feature_settings_title

)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    RadarNavKey to RADAR,
    HistoryNavKey to HISTORY,
    SettingsNavKey to SETTINGS,
)
