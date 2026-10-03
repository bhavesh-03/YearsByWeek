package com.example.yearbyweeks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF5F5F5), onPrimary = Color(0xFF111111),
    primaryContainer = Color(0xFF262626), onPrimaryContainer = Color(0xFFF5F5F5),
    secondary = Color(0xFFA3A3A3), onSecondary = Color(0xFF090909),
    secondaryContainer = Color(0xFF303030), onSecondaryContainer = Color(0xFFF5F5F5),
    tertiary = Color(0xFFF5F5F5), onTertiary = Color(0xFF111111),
    tertiaryContainer = Color(0xFF303030), onTertiaryContainer = Color(0xFFF5F5F5),
    background = Color(0xFF090909), onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF171717), onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF262626), onSurfaceVariant = Color(0xFFA3A3A3),
    surfaceContainerLowest = Color(0xFF090909), surfaceContainerLow = Color(0xFF111111),
    surfaceContainer = Color(0xFF171717), surfaceContainerHigh = Color(0xFF202020),
    surfaceContainerHighest = Color(0xFF262626), surfaceTint = Color.Transparent,
    outline = Color(0xFF666666), outlineVariant = Color(0xFF303030),
    inverseSurface = Color(0xFFF5F5F5), inverseOnSurface = Color(0xFF111111), inversePrimary = Color(0xFF111111)
)
private val LightColors = lightColorScheme(
    primary = Color(0xFF111111), onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F0F0), onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF666666), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5E5E5), onSecondaryContainer = Color(0xFF111111),
    tertiary = Color(0xFF111111), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0F0F0), onTertiaryContainer = Color(0xFF111111),
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF111111),
    surface = Color.White, onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0F0F0), onSurfaceVariant = Color(0xFF666666),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color.White, surfaceContainerHigh = Color(0xFFF5F5F5),
    surfaceContainerHighest = Color(0xFFF0F0F0), surfaceTint = Color.Transparent,
    outline = Color(0xFF666666), outlineVariant = Color(0xFFE5E5E5),
    inverseSurface = Color(0xFF111111), inverseOnSurface = Color(0xFFF5F5F5), inversePrimary = Color(0xFFF5F5F5)
)
@Composable
fun YearByWeeksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography, content = content)
}
