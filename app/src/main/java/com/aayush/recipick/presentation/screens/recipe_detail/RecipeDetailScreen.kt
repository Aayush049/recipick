package com.aayush.recipick.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aayush.recipick.presentation.components.ErrorMessage
import com.aayush.recipick.presentation.components.LoadingIndicator
import com.aayush.recipick.presentation.components.MyTopAppBar
import com.aayush.recipick.presentation.viewmodels.RecipeDetailsViewModel
import com.aayush.recipick.ui.theme.Orange

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    viewModel: RecipeDetailsViewModel = viewModel()
) {
    LaunchedEffect(recipeId) {
        viewModel.fetchRecipeDetails(recipeId)
    }

    Scaffold(
        topBar = {
            MyTopAppBar(
                title = "Recipe Details",
                onBackClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack
            )
        }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Orange.copy(0.03f))
        ) {

            when{
                viewModel.isLoading -> LoadingIndicator(strokeWidth = 1.dp)

                viewModel.errorMessage != null -> {
                    ErrorMessage(
                        errorMessage = viewModel.errorMessage,
                        onRetry = { viewModel.fetchRecipeDetails(recipeId) }
                    )
                }
            }
        }
    }
}