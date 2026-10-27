package com.aayush.recipick.domain.repository

import kotlinx.coroutines.flow.Flow

interface AiPreferencesRepository {
    fun getCustomApiKey(): Flow<String?>
    suspend fun setCustomApiKey(key: String?)
    suspend fun getEffectiveApiKey(): String
}
