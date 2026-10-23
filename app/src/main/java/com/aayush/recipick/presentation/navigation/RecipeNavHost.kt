package com.aayush.recipick.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aayush.recipick.ReciPickApp
import com.aayush.recipick.presentation.screens.add_recipe.AddRecipeScreen
import com.aayush.recipick.presentation.screens.home.HomeScreen
import com.aayush.recipick.presentation.screens.my_recipes.MyRecipesScreen
import com.aayush.recipick.presentation.screens.recipe_detail.RecipeDetailScreen
import com.aayush.recipick.presentation.screens.settings.SettingsScreen
import com.aayush.recipick.presentation.screens.user_recipe_detail.UserRecipeDetailScreen
import com.aayush.recipick.presentation.viewmodels.AddRecipeViewModel
import com.aayush.recipick.presentation.viewmodels.MyRecipesViewModel
import com.aayush.recipick.presentation.viewmodels.ThemeViewModel

@Composable
fun RecipeNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as ReciPickApp

    val themeViewModel: ThemeViewModel = viewModel(
        factory = ThemeViewModel.provideFactory(app.themePreferencesRepository)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination

    // Determine whether to show bottom bar (only on top-level destinations)
    val showBottomBar = currentRoute?.route?.let { route ->
        route.contains("HomeRoute") || route.contains("MyRecipesRoute") || route.contains("SettingsRoute")
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController, currentRoute = navBackStackEntry?.destination)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable<HomeRoute> {
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

            composable<MyRecipesRoute> {
                val myRecipesViewModel: MyRecipesViewModel = viewModel(
                    factory = MyRecipesViewModel.provideFactory(app.userRecipeRepository)
                )
                MyRecipesScreen(
                    onAddRecipeClick = { navController.navigate(AddRecipeRoute) },
                    onRecipeClick = { recipeId ->
                        navController.navigate(UserRecipeDetailRoute(recipeId))
                    },
                    viewModel = myRecipesViewModel
                )
            }

            composable<UserRecipeDetailRoute> { backStackEntry ->
                val detailRoute = backStackEntry.toRoute<UserRecipeDetailRoute>()
                UserRecipeDetailScreen(
                    recipeId = detailRoute.recipeId,
                    onBack = { navController.popBackStack() },
                    repository = app.userRecipeRepository
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(themeViewModel = themeViewModel)
            }
        }
    }
}
