package com.necdetzr.designsystem.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


private val LightColorScheme = lightColorScheme(
    primary = Cyan40,
    onPrimary = Neutral90,
    primaryContainer = Cyan90,
    onPrimaryContainer = Cyan10,
    secondary = Green40,
    onSecondary = Neutral90,
    secondaryContainer = Green90,
    onSecondaryContainer = Green10,
    background = Neutral90,
    onBackground = Neutral10,
    surface = Neutral90,
    onSurface = Neutral10,
    surfaceVariant = Neutral80,
    onSurfaceVariant = Neutral30,
    outline = Neutral40
)

private val DarkColorScheme = darkColorScheme(
    primary = Cyan80,
    onPrimary = Cyan20,
    primaryContainer = Cyan30,
    onPrimaryContainer = Cyan90,
    secondary = Green80,
    onSecondary = Green20,
    secondaryContainer = Green30,
    onSecondaryContainer = Green90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    outline = Neutral40
)
@Composable
fun BLEDeviceRadarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography =Typograph,
        content = content
    )
}
