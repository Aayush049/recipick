package com.aayush.recipick.data.repository

import com.aayush.recipick.domain.model.GeneratedRecipe
import com.aayush.recipick.domain.model.GeneratedRecipeList
import com.aayush.recipick.domain.repository.AiPreferencesRepository
import com.aayush.recipick.domain.repository.GeminiAiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json

class GeminiAiRepositoryImpl(
    private val aiPreferencesRepository: AiPreferencesRepository
) : GeminiAiRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun generateFridgeToTableRecipes(
        ingredients: List<String>,
        dietaryPreferences: List<String>,
        mealType: String
    ): Result<List<GeneratedRecipe>> = runCatching {
        val apiKey = aiPreferencesRepository.getEffectiveApiKey()
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API Key is not configured. Please enter your key in Settings.")
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.7f
            }
        )

        val prompt = buildString {
            appendLine("You are an expert executive culinary chef.")
            appendLine("Create 2 to 3 creative, delicious, and realistic recipes based PRIMARILY on these leftover ingredients:")
            appendLine("Ingredients: ${ingredients.joinToString(", ")}")
            if (dietaryPreferences.isNotEmpty()) {
                appendLine("Dietary Preferences / Constraints: ${dietaryPreferences.joinToString(", ")}")
            }
            if (mealType.isNotBlank() && mealType != "Any") {
                appendLine("Target Meal Type: $mealType")
            }
            appendLine()
            appendLine("You must respond with valid JSON matching this exact structure:")
            appendLine("""
            {
              "recipes": [
                {
                  "title": "Creative Recipe Name",
                  "description": "Brief tempting description",
                  "cuisine": "Italian",
                  "prepTimeMinutes": 10,
                  "cookTimeMinutes": 20,
                  "servings": 2,
                  "difficulty": "Easy",
                  "ingredients": ["1 cup ingredient 1", "2 tbsp ingredient 2"],
                  "instructions": ["Step 1 explanation", "Step 2 explanation"]
                }
              ]
            }
            """.trimIndent())
        }

        val response = generativeModel.generateContent(prompt)
        val responseText = response.text ?: throw IllegalStateException("Empty response from Gemini AI")

        val result = json.decodeFromString<GeneratedRecipeList>(responseText)
        result.recipes
    }
}
