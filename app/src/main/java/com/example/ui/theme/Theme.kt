package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SlateBlue80,
    onPrimary = SlateBlue20,
    primaryContainer = SlateBlue40,
    onPrimaryContainer = SlateBlue90,
    secondary = Emerald80,
    onSecondary = Emerald10,
    secondaryContainer = Emerald40,
    onSecondaryContainer = Emerald90,
    tertiary = Amber80,
    onTertiary = Amber20,
    tertiaryContainer = Amber40,
    onTertiaryContainer = Amber90,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = SlateBlue40,
    onPrimary = Color.White,
    primaryContainer = SlateBlue90,
    onPrimaryContainer = SlateBlue20,
    secondary = Emerald40,
    onSecondary = Color.White,
    secondaryContainer = Emerald90,
    onSecondaryContainer = Emerald10,
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber90,
    onTertiaryContainer = Amber20,
    background = SlateBackgroundLight,
    onBackground = SlateTextPrimary,
    surface = SlateSurfaceLight,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional clinical brand colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
