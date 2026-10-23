package com.aayush.recipick.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.aayush.recipick.domain.model.AppThemeConfig
import com.aayush.recipick.domain.model.ColorPalette
import com.aayush.recipick.domain.model.ThemeMode

// ── ORANGE ──────────────────────────────────────────────────────────────────
private val OrangeLightColorScheme = lightColorScheme(
    primary = OrangePrimaryLight,
    onPrimary = OrangeOnPrimaryLight,
    background = OrangeBackgroundLight,
    onBackground = OrangeOnBackgroundLight,
    surface = OrangeSurfaceLight,
    onSurface = OrangeOnSurfaceLight
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = OrangeOnPrimaryDark,
    background = OrangeBackgroundDark,
    onBackground = OrangeOnBackgroundDark,
    surface = OrangeSurfaceDark,
    onSurface = OrangeOnSurfaceDark
)

// ── MINT ─────────────────────────────────────────────────────────────────────
private val MintLightColorScheme = lightColorScheme(
    primary = MintPrimaryLight,
    onPrimary = MintOnPrimaryLight,
    background = MintBackgroundLight,
    onBackground = MintOnBackgroundLight,
    surface = MintSurfaceLight,
    onSurface = MintOnSurfaceLight
)

private val MintDarkColorScheme = darkColorScheme(
    primary = MintPrimaryDark,
    onPrimary = MintOnPrimaryDark,
    background = MintBackgroundDark,
    onBackground = MintOnBackgroundDark,
    surface = MintSurfaceDark,
    onSurface = MintOnSurfaceDark
)

// ── BERRY ─────────────────────────────────────────────────────────────────────
private val BerryLightColorScheme = lightColorScheme(
    primary = BerryPrimaryLight,
    onPrimary = BerryOnPrimaryLight,
    background = BerryBackgroundLight,
    onBackground = BerryOnBackgroundLight,
    surface = BerrySurfaceLight,
    onSurface = BerryOnSurfaceLight
)

private val BerryDarkColorScheme = darkColorScheme(
    primary = BerryPrimaryDark,
    onPrimary = BerryOnPrimaryDark,
    background = BerryBackgroundDark,
    onBackground = BerryOnBackgroundDark,
    surface = BerrySurfaceDark,
    onSurface = BerryOnSurfaceDark
)

// ── SAFFRON ───────────────────────────────────────────────────────────────────
private val SaffronLightColorScheme = lightColorScheme(
    primary = SaffronPrimaryLight,
    onPrimary = SaffronOnPrimaryLight,
    background = SaffronBackgroundLight,
    onBackground = SaffronOnBackgroundLight,
    surface = SaffronSurfaceLight,
    onSurface = SaffronOnSurfaceLight
)

private val SaffronDarkColorScheme = darkColorScheme(
    primary = SaffronPrimaryDark,
    onPrimary = SaffronOnPrimaryDark,
    background = SaffronBackgroundDark,
    onBackground = SaffronOnBackgroundDark,
    surface = SaffronSurfaceDark,
    onSurface = SaffronOnSurfaceDark
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

    val colorScheme = when (themeConfig.colorPalette) {
        ColorPalette.ORANGE -> if (isDark) OrangeDarkColorScheme else OrangeLightColorScheme
        ColorPalette.MINT -> if (isDark) MintDarkColorScheme else MintLightColorScheme
        ColorPalette.BERRY -> if (isDark) BerryDarkColorScheme else BerryLightColorScheme
        ColorPalette.SAFFRON -> if (isDark) SaffronDarkColorScheme else SaffronLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
