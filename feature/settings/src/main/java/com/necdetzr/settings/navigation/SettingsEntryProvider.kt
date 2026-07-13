package com.necdetzr.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.navigation.Navigator
import com.necdetzr.settings.api.SettingsNavKey

fun EntryProviderScope<NavKey>.settingsEntry(navigator: Navigator){
    entry<SettingsNavKey>{

    }
}
