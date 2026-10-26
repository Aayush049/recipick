package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.AuthUser
import com.aayush.recipick.domain.model.SyncStatus
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
    val syncStatus: StateFlow<SyncStatus> = syncRepository.syncStatus

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success("Signed in as ${user.displayName ?: user.email}")
                    syncRepository.syncRecipes()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Google sign-in failed")
                }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in both email and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signInWithEmail(email, pass)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success("Welcome back, ${user.displayName ?: user.email}")
                    syncRepository.syncRecipes()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Sign-in failed")
                }
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String?) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please provide an email and password.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUpWithEmail(email, pass, displayName)
                .onSuccess { user ->
                    _uiState.value = AuthUiState.Success("Account created for ${user.displayName ?: user.email}")
                    syncRepository.syncRecipes()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Registration failed")
                }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncRepository.syncRecipes()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            syncRepository: SyncRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository, syncRepository) as T
            }
        }
    }
}
