package com.necdetzr.history.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.history.api.HistorySearchNavKey
import com.necdetzr.history.search.HistorySearchScreen
import com.necdetzr.navigation.Navigator

fun EntryProviderScope<NavKey>.historySearch(navigator: Navigator){
    entry<HistorySearchNavKey> {
        HistorySearchScreen(
            onBackButton = { navigator.goBack() }
        )
    }
}
