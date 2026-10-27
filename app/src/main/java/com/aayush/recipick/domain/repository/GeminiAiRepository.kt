package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.GeneratedRecipe

interface GeminiAiRepository {
    suspend fun generateFridgeToTableRecipes(
        ingredients: List<String>,
        dietaryPreferences: List<String>,
        mealType: String
    ): Result<List<GeneratedRecipe>>
}
