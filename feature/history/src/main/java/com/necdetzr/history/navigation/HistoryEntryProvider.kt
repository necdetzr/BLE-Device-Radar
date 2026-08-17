package com.necdetzr.history.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.history.HistoryScreen
import com.necdetzr.history.api.HistoryNavKey
import com.necdetzr.history.api.HistorySearchNavKey
import com.necdetzr.navigation.Navigator


fun EntryProviderScope<NavKey>.historyEntry(navigator: Navigator){
    entry<HistoryNavKey>{
        HistoryScreen(
            onSearchClick = {
                navigator.navigate(HistorySearchNavKey)
            }
        )
    }
}
