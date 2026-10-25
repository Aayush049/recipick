package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.remote.dto.Recipe

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState

    data class Success(val recipe: Recipe) : RecipeDetailUiState

    sealed interface Error : RecipeDetailUiState {
        val message: String

        data class Retryable(override val message: String) : Error
        data class NonRetryable(override val message: String) : Error
    }
}
