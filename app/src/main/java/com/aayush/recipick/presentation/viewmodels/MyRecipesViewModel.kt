package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRecipesUiState(
    val recipes: List<UserRecipe> = emptyList(),
    val isLoading: Boolean = false
)

class MyRecipesViewModel(
    private val repository: UserRecipeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecipesUiState(isLoading = true))
    val uiState: StateFlow<MyRecipesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            repository.getAllUserRecipes().collect { recipes ->
                _uiState.update { it.copy(recipes = recipes, isLoading = false) }
            }
        }
    }

    fun deleteRecipe(recipe: UserRecipe) {
        viewModelScope.launch(dispatcher) {
            repository.deleteUserRecipe(recipe)
        }
    }

    companion object {
        fun provideFactory(repository: UserRecipeRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MyRecipesViewModel(repository) as T
                }
            }
    }
}
