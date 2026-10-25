package com.aayush.recipick.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aayush.recipick.presentation.components.ErrorMessage
import com.aayush.recipick.presentation.components.LoadingIndicator
import com.aayush.recipick.presentation.components.MyTopAppBar
import com.aayush.recipick.presentation.viewmodels.RecipeDetailUiState
import com.aayush.recipick.presentation.viewmodels.RecipeDetailsViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    viewModel: RecipeDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is RecipeDetailUiState.Loading -> {
                    LoadingIndicator(strokeWidth = 1.dp)
                }

                is RecipeDetailUiState.Success -> {
                    RecipeDetailContent(recipeDetails = state.recipe)
                }

                is RecipeDetailUiState.Error.Retryable -> {
                    ErrorMessage(
                        errorMessage = state.message,
                        buttonText = "Retry",
                        onRetry = { viewModel.fetchRecipeDetails(recipeId) }
                    )
                }

                is RecipeDetailUiState.Error.NonRetryable -> {
                    ErrorMessage(
                        errorMessage = state.message,
                        buttonText = "Back to Recipes",
                        onRetry = onBack
                    )
                }
            }
        }
    }
}
