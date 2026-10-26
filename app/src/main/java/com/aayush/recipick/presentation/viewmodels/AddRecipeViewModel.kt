package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.SyncRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddRecipeUiState(
    val name: String = "",
    val cuisine: String = "Italian",
    val difficulty: String = "Easy",
    val prepTimeMinutes: String = "15",
    val cookTimeMinutes: String = "20",
    val servings: String = "2",
    val caloriesPerServing: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val imageUri: String? = null,
    val nameError: String? = null,
    val ingredientsError: String? = null,
    val instructionsError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class AddRecipeViewModel(
    private val repository: UserRecipeRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val syncRepository: SyncRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateCuisine(cuisine: String) {
        _uiState.update { it.copy(cuisine = cuisine) }
    }

    fun updateDifficulty(difficulty: String) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun updatePrepTime(prepTime: String) {
        _uiState.update { it.copy(prepTimeMinutes = prepTime) }
    }

    fun updateCookTime(cookTime: String) {
        _uiState.update { it.copy(cookTimeMinutes = cookTime) }
    }

    fun updateServings(servings: String) {
        _uiState.update { it.copy(servings = servings) }
    }

    fun updateCalories(calories: String) {
        _uiState.update { it.copy(caloriesPerServing = calories) }
    }

    fun setImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun updateImageUri(uri: String?) = setImageUri(uri)

    fun addIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    ingredients = it.ingredients + trimmed,
                    ingredientsError = null
                )
            }
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            it.copy(ingredients = updated)
        }
    }

    fun addInstruction(instruction: String) {
        val trimmed = instruction.trim()
        if (trimmed.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    instructions = it.instructions + trimmed,
                    instructionsError = null
                )
            }
        }
    }

    fun removeInstruction(index: Int) {
        _uiState.update {
            val updated = it.instructions.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            it.copy(instructions = updated)
        }
    }

    fun saveRecipe() {
        val currentState = _uiState.value
        val nameErr = if (currentState.name.isBlank()) "Recipe title is required" else null
        val ingredientsErr = if (currentState.ingredients.isEmpty()) "Add at least one ingredient" else null
        val instructionsErr = if (currentState.instructions.isEmpty()) "Add at least one instruction step" else null

        if (nameErr != null || ingredientsErr != null || instructionsErr != null) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    ingredientsError = ingredientsErr,
                    instructionsError = instructionsErr
                )
            }
            return
        }

        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val recipe = UserRecipe(
                    name = currentState.name.trim(),
                    ingredients = currentState.ingredients,
                    instructions = currentState.instructions,
                    prepTimeMinutes = currentState.prepTimeMinutes.toIntOrNull() ?: 15,
                    cookTimeMinutes = currentState.cookTimeMinutes.toIntOrNull() ?: 20,
                    servings = currentState.servings.toIntOrNull() ?: 2,
                    difficulty = currentState.difficulty,
                    cuisine = currentState.cuisine,
                    caloriesPerServing = currentState.caloriesPerServing.toIntOrNull() ?: 0,
                    imageUri = currentState.imageUri
                )
                repository.insertUserRecipe(recipe)
                runCatching { syncRepository?.syncRecipes() }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: UserRecipeRepository,
            syncRepository: SyncRepository? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddRecipeViewModel(
                        repository = repository,
                        syncRepository = syncRepository
                    ) as T
                }
            }
    }
}
