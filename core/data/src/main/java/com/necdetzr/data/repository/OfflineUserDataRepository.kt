package com.necdetzr.data.repository

import com.necdetzr.datastore.BlePreferencesDataSource
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineUserDataRepository @Inject constructor(
    private val blePreferencesDataSource: BlePreferencesDataSource
) : UserDataRepository{
    override val userPreferences: Flow<UserPreferences> =
        blePreferencesDataSource.userData
    override suspend fun setScanPeriod(scanPeriod: Long) {
        blePreferencesDataSource.updateScanPeriod(scanPeriod)
    }

    override suspend fun setSortType(sortType: SortType) {
        blePreferencesDataSource.updateSortType(sortType)
    }

    override suspend fun setThemeConfig(themeConfig: ThemeConfig) {
        blePreferencesDataSource.updateThemeConfig(themeConfig)
    }

    override suspend fun setRssiRange(rssiRange: Int) {
        blePreferencesDataSource.updateRssiRange(rssiRange)
    }
}
