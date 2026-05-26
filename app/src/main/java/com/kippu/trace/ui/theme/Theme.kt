package com.kippu.trace.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onTertiary = Primary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFFE1E2E4),
    onSurfaceVariant = Color(0xFF44474E),
    primaryContainer = Color(0xFFE1E2E4),
    onPrimaryContainer = Color(0xFF1A1A1A),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    tertiaryContainer = Color(0xFFE1E2E4),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onTertiary = DarkBackground,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    primaryContainer = Color(0xFF333333),
    onPrimaryContainer = Color(0xFFE0E0E0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
    tertiaryContainer = Color(0xFF333333),
)

@Composable
fun KIPPU_TraceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
