package com.aayush.recipick.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedRecipe(
    val title: String,
    val description: String = "",
    val cuisine: String = "Fusion",
    val prepTimeMinutes: Int = 15,
    val cookTimeMinutes: Int = 20,
    val servings: Int = 2,
    val difficulty: String = "Easy",
    val ingredients: List<String>,
    val instructions: List<String>
)

@Serializable
data class GeneratedRecipeList(
    val recipes: List<GeneratedRecipe> = emptyList()
)
