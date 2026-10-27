package com.aayush.recipick.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aayush.recipick.BuildConfig
import com.aayush.recipick.domain.repository.AiPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.aiDataStore by preferencesDataStore(name = "ai_preferences")

class AiPreferencesRepositoryImpl(
    private val context: Context
) : AiPreferencesRepository {

    private val customApiKeyKey = stringPreferencesKey("custom_gemini_api_key")

    override fun getCustomApiKey(): Flow<String?> =
        context.aiDataStore.data.map { prefs ->
            prefs[customApiKeyKey]?.takeIf { it.isNotBlank() }
        }

    override suspend fun setCustomApiKey(key: String?) {
        context.aiDataStore.edit { prefs ->
            if (key.isNullOrBlank()) {
                prefs.remove(customApiKeyKey)
            } else {
                prefs[customApiKeyKey] = key.trim()
            }
        }
    }

    override suspend fun getEffectiveApiKey(): String {
        val customKey = getCustomApiKey().first()
        return if (!customKey.isNullOrBlank()) {
            customKey
        } else {
            BuildConfig.GEMINI_API_KEY
        }
    }
}
