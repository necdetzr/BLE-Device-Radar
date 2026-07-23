package com.necdetzr.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.datastore.BlePreferencesDataSource
import com.necdetzr.model.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val dataStore: BlePreferencesDataSource
) : ViewModel(){

    val settingsUiState: StateFlow<SettingsViewState> = userDataRepository.userPreferences
        .map { userData->
            SettingsViewState(
                rssi = userData.rssiRange,
                scanPeriod = userData.scanPeriod,
                theme = userData.themeConfig,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsViewState(isLoading = true)
        )

     fun setRssiRange(newRssi:Int){
        viewModelScope.launch {
            userDataRepository.setRssiRange(newRssi)

        }
    }
     fun setScanPeriod(newPeriod:Long){
        viewModelScope.launch {
            userDataRepository.setScanPeriod(newPeriod)
        }
    }
     fun setTheme(theme: ThemeConfig){
        viewModelScope.launch {
            userDataRepository.setThemeConfig(theme)
        }
    }

}
