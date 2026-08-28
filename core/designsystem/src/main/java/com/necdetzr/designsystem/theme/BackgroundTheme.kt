package com.necdetzr.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


@Immutable
data class BackgroundTheme(
    val color: Color = Color.Unspecified,
    val tonalElevation: Color = Color.Unspecified
)

val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }
