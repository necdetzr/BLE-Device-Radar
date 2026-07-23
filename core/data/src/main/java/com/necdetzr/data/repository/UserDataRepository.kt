package com.necdetzr.data.repository

import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setSortType(sortType: SortType)
    suspend fun setScanPeriod(scanPeriod: Long)
    suspend fun setRssiRange(rssiRange: Int)
}
