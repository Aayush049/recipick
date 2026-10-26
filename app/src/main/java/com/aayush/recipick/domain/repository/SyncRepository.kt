package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.SyncStatus
import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    val syncStatus: StateFlow<SyncStatus>
    suspend fun syncRecipes(): Result<Unit>
}
