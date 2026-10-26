package com.aayush.recipick.data.repository

import com.aayush.recipick.domain.model.AuthUser
import com.aayush.recipick.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(firebaseAuth.currentUser?.toDomain())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser?.toDomain()
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user?.toDomain()
                ?: throw IllegalStateException("Firebase returned null user upon Google sign-in.")
            _currentUser.value = user
            user
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return runCatching {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user?.toDomain()
                ?: throw IllegalStateException("Firebase returned null user upon Email sign-in.")
            _currentUser.value = user
            user
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        return runCatching {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase returned null user upon account creation.")

            if (!displayName.isNullOrBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }

            val user = firebaseUser.toDomain(overrideName = displayName?.trim())
            _currentUser.value = user
            user
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            firebaseAuth.currentUser?.delete()?.await()
            _currentUser.value = null
        }
    }

    private fun FirebaseUser.toDomain(overrideName: String? = null): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = overrideName ?: displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous
        )
    }
}
