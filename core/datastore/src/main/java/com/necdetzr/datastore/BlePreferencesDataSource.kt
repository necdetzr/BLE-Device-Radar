package com.necdetzr.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BlePreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val SORT_TYPE = stringPreferencesKey("sort_type")
        val SCAN_PERIOD = longPreferencesKey("scan_period")
    }
    val userData: Flow<UserPreferences> = dataStore.data.map { preferences->
        UserPreferences(
            themeConfig = try{
                val themeValue = preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
                ThemeConfig.valueOf(themeValue)
            }catch (e: IllegalStateException){
                ThemeConfig.FOLLOW_SYSTEM
            },
            sortType = try {
                val sortValue = preferences[PreferencesKeys.SORT_TYPE] ?: SortType.BY_RSSI.name
                SortType.valueOf(sortValue)
            }catch (e: IllegalStateException){
                SortType.BY_RSSI
            },
            scanPeriod = preferences[PreferencesKeys.SCAN_PERIOD] ?: 15000L

        )
    }
    suspend fun updateThemeConfig(themeConfig: ThemeConfig){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.THEME_CONFIG] = themeConfig.name
        }
    }
    suspend fun updateSortType(sortType: SortType){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.SORT_TYPE] = sortType.name
        }
    }
    suspend fun updateScanPeriod(scanPeriod:Long){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.SCAN_PERIOD] = scanPeriod
        }
    }
}
