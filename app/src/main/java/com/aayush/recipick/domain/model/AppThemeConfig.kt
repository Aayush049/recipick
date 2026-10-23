package com.aayush.recipick.domain.model

data class AppThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.ORANGE
)
