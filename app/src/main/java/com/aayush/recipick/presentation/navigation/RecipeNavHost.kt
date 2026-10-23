package com.aayush.recipick.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aayush.recipick.presentation.screens.home.HomeScreen
import com.aayush.recipick.presentation.screens.recipe_detail.RecipeDetailScreen

@Composable
fun RecipeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ){
        composable<HomeRoute>{
            HomeScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(RecipeDetailsRoute(recipeId))
                }
            )
        }

        composable<RecipeDetailsRoute> { backStackEntry ->

            val detailRoute = backStackEntry.toRoute<RecipeDetailsRoute>()

            RecipeDetailScreen(
                recipeId = detailRoute.recipeId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}