package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.AuthUser
import com.aayush.recipick.domain.model.SyncStatus
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    var shouldFail = false
    var lastIdTokenReceived: String? = null

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        lastIdTokenReceived = idToken
        return if (shouldFail) {
            Result.failure(Exception("Google Sign-In failed"))
        } else {
            val user = AuthUser(uid = "google_123", email = "chef@google.com", displayName = "Google Chef")
            _currentUser.value = user
            Result.success(user)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return if (shouldFail) {
            Result.failure(Exception("Invalid credentials"))
        } else {
            val user = AuthUser(uid = "email_123", email = email, displayName = "Email Chef")
            _currentUser.value = user
            Result.success(user)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        return if (shouldFail) {
            Result.failure(Exception("Email already in use"))
        } else {
            val user = AuthUser(uid = "new_123", email = email, displayName = displayName ?: "New Chef")
            _currentUser.value = user
            Result.success(user)
        }
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }
}

class FakeSyncRepository : SyncRepository {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    var syncCallCount = 0

    override suspend fun syncRecipes(): Result<Unit> {
        syncCallCount++
        _syncStatus.value = SyncStatus.Success(1000L)
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var syncRepository: FakeSyncRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        syncRepository = FakeSyncRepository()
        viewModel = AuthViewModel(authRepository, syncRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_isGuestUser_and_idle() {
        assertNull(viewModel.currentUser.value)
        assertEquals(SyncStatus.Idle, viewModel.syncStatus.value)
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun testSignInWithEmail_success_updatesUiState_and_triggersSync() = runTest {
        viewModel.signInWithEmail("chef@recipick.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Success)
        assertEquals("email_123", viewModel.currentUser.value?.uid)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun testSignInWithEmail_failure_updatesErrorUiState() = runTest {
        authRepository.shouldFail = true
        viewModel.signInWithEmail("wrong@recipick.com", "wrongpass")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun testSignUpWithEmail_shortPassword_showsValidationError() = runTest {
        viewModel.signUpWithEmail("chef@recipick.com", "123", "Chef Ayush")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AuthUiState.Error)
        assertEquals(
            "Password must be at least 6 characters.",
            (viewModel.uiState.value as AuthUiState.Error).message
        )
    }

    @Test
    fun testSignInWithGoogle_success_updatesUser_and_syncs() = runTest {
        viewModel.signInWithGoogleToken("test_id_token")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("test_id_token", authRepository.lastIdTokenReceived)
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
        assertEquals("google_123", viewModel.currentUser.value?.uid)
        assertEquals(1, syncRepository.syncCallCount)
    }

    @Test
    fun testSignOut_clearsCurrentUser() = runTest {
        viewModel.signInWithEmail("chef@recipick.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("email_123", viewModel.currentUser.value?.uid)

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.currentUser.value)
        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun testSyncNow_triggersSyncRecipes() = runTest {
        viewModel.syncNow()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, syncRepository.syncCallCount)
        assertEquals(SyncStatus.Success(1000L), viewModel.syncStatus.value)
    }
}
