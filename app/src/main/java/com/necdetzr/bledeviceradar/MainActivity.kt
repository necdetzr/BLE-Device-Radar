package com.necdetzr.bledeviceradar

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.SystemBarStyle.Companion.light
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.trace
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.necdetzr.bledeviceradar.ui.BleApp
import com.necdetzr.bledeviceradar.ui.rememberBleAppState
import com.necdetzr.bledeviceradar.util.isSystemInDarkTheme
import com.necdetzr.designsystem.theme.BLEDeviceRadarTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var themeSetting by mutableStateOf(
            ThemeSettings(
                darkTheme = resources.configuration.isSystemInDarkTheme
            )
        )
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                combine(
                    isSystemInDarkTheme(),
                    viewModel.uiState,
                ){systemDark,uiState->
                    ThemeSettings(
                        darkTheme = uiState.shouldUseDarkTheme(systemDark)
                    )
                }.onEach { themeSetting = it }
                    .map { it.darkTheme }
                    .distinctUntilChanged()
                    .collect { darkTheme->
                        trace("setTheme"){
                            enableEdgeToEdge(
                                statusBarStyle = SystemBarStyle.auto(
                                    lightScrim = Color.TRANSPARENT,
                                    darkScrim = Color.TRANSPARENT
                                ){darkTheme},
                                navigationBarStyle = SystemBarStyle.auto(
                                    lightScrim = lightScrim,
                                    darkScrim = darkScrim
                                ){darkTheme}
                            )
                        }
                    }

            }
        }
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }
        setContent {
            val appState = rememberBleAppState()
            BLEDeviceRadarTheme(
                darkTheme =themeSetting.darkTheme,
            ) {
                BleApp(
                    appState
                )
            }
        }
    }
}
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

data class ThemeSettings(
    var darkTheme:Boolean
)

