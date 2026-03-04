package com.faheem.pocketapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrangePrimary,
    onPrimary = DarkText,
    primaryContainer = DarkOrangeContainer,
    onPrimaryContainer = LightText,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkBackground,
    onSurface = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = WhiteSurface,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = DarkText,
    background = LightBackground,
    onBackground = DarkText,
    surface = WhiteSurface,
    onSurface = DarkText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}