package com.necdetzr.radar.navigation

import android.annotation.SuppressLint
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.navigation.Navigator
import com.necdetzr.radar.RadarScreen
import com.necdetzr.radar.api.RadarNavKey

@SuppressLint("VisibleForTests")
fun EntryProviderScope<NavKey>.radarEntry(navigator: Navigator){
    entry<RadarNavKey> {
        RadarScreen(
            onDeviceClick = {}
        )
    }
}
