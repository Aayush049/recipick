package com.aayush.recipick.domain.model

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Success(val lastSyncedAt: Long = System.currentTimeMillis()) : SyncStatus
    data class Error(val message: String) : SyncStatus
}
