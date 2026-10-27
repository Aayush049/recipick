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
object PlannerRoute

@Serializable
object FridgeToTableRoute

@Serializable
data class CommunityRecipeDetailRoute(val recipeId: String)

@Serializable
object SettingsRoute
