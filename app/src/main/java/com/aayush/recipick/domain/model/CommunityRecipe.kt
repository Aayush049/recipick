package com.aayush.recipick.domain.model

data class CommunityRecipe(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "Community Chef",
    val name: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servings: Int = 1,
    val difficulty: String = "Medium",
    val cuisine: String = "General",
    val caloriesPerServing: Int = 0,
    val imageUrl: String? = null,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val tipCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
