package com.necdetzr.radar.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.navigation.Navigator
import com.necdetzr.radar.RadarRoute
import com.necdetzr.radar.api.RadarNavKey
@Suppress("UnusedParameter")
fun EntryProviderScope<NavKey>.radarEntry(navigator: Navigator){
    entry<RadarNavKey> {
        RadarRoute()
    }
}
