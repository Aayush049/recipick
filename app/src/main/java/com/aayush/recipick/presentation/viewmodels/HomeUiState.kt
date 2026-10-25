package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.remote.dto.Recipe

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val recipes: List<Recipe>,
        val categories: List<String>,
        val selectedCategory: String
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}
