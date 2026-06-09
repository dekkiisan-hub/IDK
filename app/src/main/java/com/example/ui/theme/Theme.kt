package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimary,
    secondary = GeoSecondary,
    tertiary = GeoTertiary,
    background = GeoBackground,
    surface = GeoSurface,
    onPrimary = OnGeoPrimary,
    onSecondary = OnGeoSecondary,
    onTertiary = Color.White,
    onBackground = OnGeoBackground,
    onSurface = OnGeoSurface,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = OnGeoBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force Light theme for the Geometric Balance aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
