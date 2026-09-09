package com.necdetzr.history.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object HistoryNavKey : NavKey

@Serializable
data class HistorySearchNavKey(
    val initialQuery: String = "",
    val devicesOnly: Boolean = false
): NavKey
