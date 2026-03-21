package com.sathsara.ecbtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,
    primary = Cyan,
    secondary = Green,
    tertiary = Purple,
    error = Red,
    onPrimary = OnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,
    primary = Cyan,
    secondary = Green,
    tertiary = Purple,
    error = Red,
    onPrimary = OnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
)

val EcbShapes = Shapes(
    small = RoundedCornerShape(6.dp),    // Chips
    medium = RoundedCornerShape(8.dp),   // Buttons
    large = RoundedCornerShape(12.dp),   // Cards
)

@Composable
fun EcbTrackerTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EcbTypography,
        shapes = EcbShapes,
        content = content
    )
}
