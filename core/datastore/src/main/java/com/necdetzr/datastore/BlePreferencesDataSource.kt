package com.necdetzr.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.necdetzr.model.SortType
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class BlePreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val SORT_TYPE = stringPreferencesKey("sort_type")
        val SCAN_PERIOD = longPreferencesKey("scan_period")
        val RSSI_RANGE = intPreferencesKey("rssi_range")
    }
    val userData: Flow<UserPreferences> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map { preferences->
            val themeConfig = preferences[PreferencesKeys.THEME_CONFIG]
                ?.let { value->
                    ThemeConfig.entries.firstOrNull { theme->
                        theme.name == value
                    }
                } ?: ThemeConfig.FOLLOW_SYSTEM

            val sortType = preferences[PreferencesKeys.SORT_TYPE]
                ?.let { value->
                    SortType.entries.firstOrNull{type->
                        type.name == value
                    }
                }?: SortType.BY_NAME
            UserPreferences(
                themeConfig = themeConfig,
                sortType = sortType,
                scanPeriod = preferences[PreferencesKeys.SCAN_PERIOD] ?: 30_000L,
                rssiRange = preferences[PreferencesKeys.RSSI_RANGE] ?: -90
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
    suspend fun updateRssiRange(rssiRange:Int){
        dataStore.edit { preferences->
            preferences[PreferencesKeys.RSSI_RANGE] = rssiRange
        }
    }
}
