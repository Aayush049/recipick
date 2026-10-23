package com.aayush.recipick.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aayush.recipick.domain.model.AppThemeConfig
import com.aayush.recipick.domain.model.ColorPalette
import com.aayush.recipick.domain.model.ThemeMode
import com.aayush.recipick.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

class ThemePreferencesRepositoryImpl(
    private val context: Context
) : ThemePreferencesRepository {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val colorPaletteKey = stringPreferencesKey("color_palette")

    override fun getThemeConfig(): Flow<AppThemeConfig> =
        context.dataStore.data.map { prefs ->
            val themeMode = prefs[themeModeKey]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
            val colorPalette = prefs[colorPaletteKey]
                ?.let { runCatching { ColorPalette.valueOf(it) }.getOrNull() }
                ?: ColorPalette.ORANGE
            AppThemeConfig(themeMode = themeMode, colorPalette = colorPalette)
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    override suspend fun setColorPalette(palette: ColorPalette) {
        context.dataStore.edit { prefs ->
            prefs[colorPaletteKey] = palette.name
        }
    }
}
