package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<AuthUser>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
