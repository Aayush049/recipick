package com.aayush.recipick.domain.model

data class MealPlanItem(
    val id: Long = 0,
    val userId: String? = null,
    val date: String, // ISO-8601 Date string: YYYY-MM-DD
    val slot: MealSlot,
    val recipeId: Int? = null,
    val recipeTitle: String,
    val recipeImageUrl: String? = null,
    val isUserRecipe: Boolean = false,
    val customNote: String? = null,
    val ingredients: List<String> = emptyList()
)
