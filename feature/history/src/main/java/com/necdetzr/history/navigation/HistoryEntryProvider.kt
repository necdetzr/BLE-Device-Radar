package com.necdetzr.history.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.necdetzr.history.HistoryScreen
import com.necdetzr.history.api.HistoryNavKey


fun EntryProviderScope<NavKey>.historyEntry(){
    entry<HistoryNavKey>{
        HistoryScreen()
    }
}
