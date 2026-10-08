package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OTTDarkColorScheme = darkColorScheme(
    primary = CinemaRed,
    onPrimary = Color.White,
    primaryContainer = CinemaRedDark,
    onPrimaryContainer = Color.White,
    secondary = CinemaGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382E06),
    onSecondaryContainer = CinemaGold,
    tertiary = CinemaCyan,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = StatusError,
    onError = Color.White
)

@Composable
fun TTMovieBoxTheme(
    content: @Composable () -> Unit
) {
    // OTT experience is always rich cinema dark
    MaterialTheme(
        colorScheme = OTTDarkColorScheme,
        typography = Typography,
        content = content
    )
}
