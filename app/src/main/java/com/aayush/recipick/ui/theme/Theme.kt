package com.aayush.recipick.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.aayush.recipick.domain.model.AppThemeConfig
import com.aayush.recipick.domain.model.ThemeMode

private val WarmOrangeLightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = OrangeOnPrimaryLight,
    primaryContainer = OrangePrimaryContainerLight,
    onPrimaryContainer = OrangeOnPrimaryContainerLight,
    background = OrangeBackgroundLight,
    onBackground = OrangeOnBackgroundLight,
    surface = OrangeSurfaceLight,
    onSurface = OrangeOnSurfaceLight,
    surfaceVariant = OrangeSurfaceVariantLight,
    onSurfaceVariant = OrangeOnSurfaceVariantLight,
    outline = OrangeOutlineLight,
    outlineVariant = OrangeOutlineVariantLight
)

private val WarmOrangeDarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = OrangeOnPrimaryDark,
    primaryContainer = OrangePrimaryContainerDark,
    onPrimaryContainer = OrangeOnPrimaryContainerDark,
    background = OrangeBackgroundDark,
    onBackground = OrangeOnBackgroundDark,
    surface = OrangeSurfaceDark,
    onSurface = OrangeOnSurfaceDark,
    surfaceVariant = OrangeSurfaceVariantDark,
    onSurfaceVariant = OrangeOnSurfaceVariantDark,
    outline = OrangeOutlineDark,
    outlineVariant = OrangeOutlineVariantDark
)

@Composable
fun RecipickTheme(
    themeConfig: AppThemeConfig = AppThemeConfig(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) WarmOrangeDarkColorScheme else WarmOrangeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
