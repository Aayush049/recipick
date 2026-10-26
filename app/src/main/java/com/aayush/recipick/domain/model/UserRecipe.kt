package com.aayush.recipick.domain.model

data class UserRecipe(
    val id: Int = 0,
    val userId: String? = null,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val cuisine: String,
    val caloriesPerServing: Int = 0,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
