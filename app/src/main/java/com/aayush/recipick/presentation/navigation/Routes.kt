package com.aayush.recipick.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class RecipeDetailsRoute(val recipeId: Int)

@Serializable
object AddRecipeRoute

@Serializable
object MyRecipesRoute

@Serializable
data class UserRecipeDetailRoute(val recipeId: Int)

@Serializable
object SettingsRoute
