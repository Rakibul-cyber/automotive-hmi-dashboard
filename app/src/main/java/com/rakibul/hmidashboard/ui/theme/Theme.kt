package com.rakibul.hmidashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AutomotivePrimary,
    secondary = AutomotiveSecondary,
    tertiary = AutomotiveSuccess,
    background = AutomotiveBackground,
    surface = AutomotiveSurface,
    surfaceVariant = AutomotiveSurfaceVariant,
    onPrimary = AutomotiveBackground,
    onSecondary = AutomotiveBackground,
    onBackground = AutomotiveText,
    onSurface = AutomotiveText,
    onSurfaceVariant = AutomotiveTextMuted,
    error = AutomotiveDanger
)

@Composable
fun HMIDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AutomotiveTypography,
        content = content
    )
}