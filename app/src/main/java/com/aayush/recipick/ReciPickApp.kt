package com.aayush.recipick

import android.app.Application
import com.aayush.recipick.data.local.db.ReciPickDatabase
import com.aayush.recipick.data.repository.AiPreferencesRepositoryImpl
import com.aayush.recipick.data.repository.AuthRepositoryImpl
import com.aayush.recipick.data.repository.CommunityRepositoryImpl
import com.aayush.recipick.data.repository.GeminiAiRepositoryImpl
import com.aayush.recipick.data.repository.GroceryRepositoryImpl
import com.aayush.recipick.data.repository.MealPlanRepositoryImpl
import com.aayush.recipick.data.repository.SyncRepositoryImpl
import com.aayush.recipick.data.repository.ThemePreferencesRepositoryImpl
import com.aayush.recipick.data.repository.UserRecipeRepositoryImpl
import com.aayush.recipick.domain.repository.AiPreferencesRepository
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.CommunityRepository
import com.aayush.recipick.domain.repository.GeminiAiRepository
import com.aayush.recipick.domain.repository.GroceryRepository
import com.aayush.recipick.domain.repository.MealPlanRepository
import com.aayush.recipick.domain.repository.SyncRepository
import com.aayush.recipick.domain.repository.ThemePreferencesRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import com.google.firebase.firestore.FirebaseFirestore

class ReciPickApp : Application() {
    val database: ReciPickDatabase by lazy { ReciPickDatabase.getDatabase(this) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    val userRecipeRepository: UserRecipeRepository by lazy {
        UserRecipeRepositoryImpl(
            userRecipeDao = database.userRecipeDao(),
            authRepository = authRepository,
            firestore = FirebaseFirestore.getInstance()
        )
    }

    val mealPlanRepository: MealPlanRepository by lazy {
        MealPlanRepositoryImpl(
            mealPlanDao = database.mealPlanDao(),
            authRepository = authRepository
        )
    }

    val groceryRepository: GroceryRepository by lazy {
        GroceryRepositoryImpl(
            groceryDao = database.groceryDao(),
            authRepository = authRepository
        )
    }

    val themePreferencesRepository: ThemePreferencesRepository by lazy {
        ThemePreferencesRepositoryImpl(this)
    }

    val aiPreferencesRepository: AiPreferencesRepository by lazy {
        AiPreferencesRepositoryImpl(this)
    }

    val geminiAiRepository: GeminiAiRepository by lazy {
        GeminiAiRepositoryImpl(aiPreferencesRepository)
    }

    val communityRepository: CommunityRepository by lazy {
        CommunityRepositoryImpl(
            authRepository = authRepository,
            userRecipeDao = database.userRecipeDao(),
            firestore = FirebaseFirestore.getInstance()
        )
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(
            authRepository = authRepository,
            userRecipeDao = database.userRecipeDao()
        )
    }
}
