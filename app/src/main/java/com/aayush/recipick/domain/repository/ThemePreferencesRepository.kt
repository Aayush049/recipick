package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.AppThemeConfig
import com.aayush.recipick.domain.model.ColorPalette
import com.aayush.recipick.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferencesRepository {
    fun getThemeConfig(): Flow<AppThemeConfig>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setColorPalette(palette: ColorPalette)
}
