package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = TealAccent,
    tertiary = SoftTurquoise,
    background = BgDeepDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFF0C1919),
    onSecondary = Color.White,
    onTertiary = Color(0xFF0C1919),
    onBackground = Color(0xFFE2EBEB),
    onSurface = Color(0xFFE2EBEB),
    primaryContainer = TealPrimary,
    secondaryContainer = Color(0xFF122E2E)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = GoldPrimary,
    background = BgLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E293B), // Elegant slate-800 text color 
    onSurface = Color(0xFF1E293B),     // Elegant slate-800 text color
    primaryContainer = SoftTurquoise,  // soft teal container background (0xFFE0F2F2)
    secondaryContainer = GoldLight    // soft luxury gold container background (0xFFFFF9E6)
)

@Composable
fun AarakshaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
