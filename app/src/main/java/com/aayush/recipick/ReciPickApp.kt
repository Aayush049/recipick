package com.aayush.recipick

import android.app.Application
import com.aayush.recipick.data.local.db.ReciPickDatabase
import com.aayush.recipick.data.repository.AuthRepositoryImpl
import com.aayush.recipick.data.repository.SyncRepositoryImpl
import com.aayush.recipick.data.repository.ThemePreferencesRepositoryImpl
import com.aayush.recipick.data.repository.UserRecipeRepositoryImpl
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.SyncRepository
import com.aayush.recipick.domain.repository.ThemePreferencesRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository

class ReciPickApp : Application() {
    val database: ReciPickDatabase by lazy { ReciPickDatabase.getDatabase(this) }

    val userRecipeRepository: UserRecipeRepository by lazy {
        UserRecipeRepositoryImpl(database.userRecipeDao())
    }

    val themePreferencesRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepositoryImpl(this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(
            authRepository = authRepository,
            userRecipeDao = database.userRecipeDao()
        )
    }
}
