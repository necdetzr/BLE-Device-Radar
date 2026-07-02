package com.necdetzr.data.repository

import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig

interface UserDataRepository {
    suspend fun setThemeConfig(themeConfig: ThemeConfig)
    suspend fun setSortType(sortType: SortType)
    suspend fun setScanPeriod(scanPeriod: Long)
}
