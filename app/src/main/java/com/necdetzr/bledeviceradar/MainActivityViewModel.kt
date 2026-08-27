package com.necdetzr.bledeviceradar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necdetzr.data.repository.UserDataRepository
import com.necdetzr.model.ThemeConfig
import com.necdetzr.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository
) : ViewModel(){
    val uiState : StateFlow<MainActivityUiState> = userDataRepository.userPreferences.map {
        MainActivityUiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )
}

sealed interface MainActivityUiState{
    data object Loading: MainActivityUiState
    data class Success(val userPreferences: UserPreferences) : MainActivityUiState{
        override fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) =
            when(userPreferences.themeConfig){
                ThemeConfig.DARK -> true
                ThemeConfig.LIGHT -> false
                ThemeConfig.FOLLOW_SYSTEM -> isSystemDarkTheme
            }

    }
    fun shouldUseDarkTheme(isSystemDarkTheme: Boolean) = isSystemDarkTheme
    fun shouldKeepSplashScreen() = this is Loading
}
