package com.plazaorbita.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlazaOrange = Color(0xFFE85A2A)
private val PlazaOrangeDark = Color(0xFFC9481F)
private val PlazaBackground = Color(0xFFF8F8FA)
private val PlazaSurface = Color(0xFFFFFFFF)
private val PlazaSurfaceVariant = Color(0xFFF2F2F5)
private val PlazaText = Color(0xFF1A1A1A)
private val PlazaMuted = Color(0xFF6F6F73)
private val PlazaOrangeSoft = Color(0xFFFCE1D6)

private val LightColors = lightColorScheme(
    primary = PlazaOrange,
    onPrimary = Color.White,
    primaryContainer = PlazaOrangeSoft,
    onPrimaryContainer = Color(0xFF6B2816),
    secondary = PlazaMuted,
    onSecondary = Color.White,
    secondaryContainer = PlazaSurfaceVariant,
    onSecondaryContainer = PlazaText,
    tertiary = PlazaOrange,
    background = PlazaBackground,
    onBackground = PlazaText,
    surface = PlazaSurface,
    onSurface = PlazaText,
    surfaceVariant = PlazaSurfaceVariant,
    onSurfaceVariant = PlazaMuted,
    outline = Color(0xFFD9D9DE)
)

private val DarkColors = darkColorScheme(
    primary = PlazaOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7A321B),
    onPrimaryContainer = Color(0xFFFFDBCB),
    secondary = Color(0xFFB8B8BE),
    onSecondary = Color(0xFF202024),
    secondaryContainer = Color(0xFF36363B),
    onSecondaryContainer = Color.White,
    tertiary = PlazaOrange,
    background = Color(0xFF151517),
    onBackground = Color.White,
    surface = Color(0xFF1C1C1F),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF303035),
    onSurfaceVariant = Color(0xFFC8C8CD),
    outline = Color(0xFF55555C)
)

@Composable
fun PlazaOrbitaTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
