package com.example.languagepartner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandLime,
    onPrimary = Color.Black,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = BrandLime,
    secondary = BrandLime,
    onSecondary = Color.Black,
    background = BrandBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun LanguagePartnerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
