package com.aayush.recipick.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aayush.recipick.ReciPickApp
import com.aayush.recipick.presentation.screens.add_recipe.AddRecipeScreen
import com.aayush.recipick.presentation.screens.home.HomeScreen
import com.aayush.recipick.presentation.screens.recipe_detail.RecipeDetailScreen
import com.aayush.recipick.presentation.viewmodels.AddRecipeViewModel

@Composable
fun RecipeNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as ReciPickApp

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ){
        composable<HomeRoute>{
            HomeScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(RecipeDetailsRoute(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(AddRecipeRoute)
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

        composable<AddRecipeRoute> {
            val addRecipeViewModel: AddRecipeViewModel = viewModel(
                factory = AddRecipeViewModel.provideFactory(app.userRecipeRepository)
            )
            AddRecipeScreen(
                onBack = { navController.popBackStack() },
                viewModel = addRecipeViewModel
            )
        }
    }
}
