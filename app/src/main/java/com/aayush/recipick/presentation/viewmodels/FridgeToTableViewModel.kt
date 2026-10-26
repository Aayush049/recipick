package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.GeneratedRecipe
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.model.MealSlot
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.AiPreferencesRepository
import com.aayush.recipick.domain.repository.GeminiAiRepository
import com.aayush.recipick.domain.repository.MealPlanRepository
import com.aayush.recipick.domain.repository.SyncRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FridgeToTableUiState(
    val ingredients: List<String> = emptyList(),
    val dietaryPreferences: Set<String> = emptySet(),
    val selectedMealType: String = "Any",
    val isGenerating: Boolean = false,
    val generatedRecipes: List<GeneratedRecipe> = emptyList(),
    val error: String? = null,
    val savedRecipeTitles: Set<String> = emptySet(),
    val hasApiKey: Boolean = true
)

class FridgeToTableViewModel(
    private val geminiAiRepository: GeminiAiRepository,
    private val aiPreferencesRepository: AiPreferencesRepository,
    private val userRecipeRepository: UserRecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val syncRepository: SyncRepository? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridgeToTableUiState())
    val uiState: StateFlow<FridgeToTableUiState> = _uiState.asStateFlow()

    init {
        checkApiKey()
    }

    fun checkApiKey() {
        viewModelScope.launch(dispatcher) {
            val key = aiPreferencesRepository.getEffectiveApiKey()
            _uiState.update { it.copy(hasApiKey = key.isNotBlank()) }
        }
    }

    fun addIngredient(ingredient: String) {
        val trimmed = ingredient.trim()
        if (trimmed.isNotBlank() && !_uiState.value.ingredients.any { it.equals(trimmed, ignoreCase = true) }) {
            _uiState.update { it.copy(ingredients = it.ingredients + trimmed) }
        }
    }

    fun removeIngredient(ingredient: String) {
        _uiState.update { it.copy(ingredients = it.ingredients - ingredient) }
    }

    fun toggleDietary(pref: String) {
        _uiState.update {
            val current = it.dietaryPreferences
            val updated = if (current.contains(pref)) current - pref else current + pref
            it.copy(dietaryPreferences = updated)
        }
    }

    fun selectMealType(mealType: String) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun generateRecipes() {
        val ingredients = _uiState.value.ingredients
        if (ingredients.isEmpty()) {
            _uiState.update { it.copy(error = "Please add at least one ingredient.") }
            return
        }

        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            geminiAiRepository.generateFridgeToTableRecipes(
                ingredients = ingredients,
                dietaryPreferences = _uiState.value.dietaryPreferences.toList(),
                mealType = _uiState.value.selectedMealType
            ).onSuccess { recipes ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedRecipes = recipes,
                        error = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = err.localizedMessage ?: "Failed to generate recipes with Gemini."
                    )
                }
            }
        }
    }

    fun saveRecipeToMyCookbook(recipe: GeneratedRecipe, onSuccess: () -> Unit = {}) {
        viewModelScope.launch(dispatcher) {
            val userRecipe = UserRecipe(
                name = recipe.title,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                prepTimeMinutes = recipe.prepTimeMinutes,
                cookTimeMinutes = recipe.cookTimeMinutes,
                servings = recipe.servings,
                difficulty = recipe.difficulty,
                cuisine = recipe.cuisine
            )
            userRecipeRepository.insertUserRecipe(userRecipe)
            runCatching { syncRepository?.syncRecipes() }
            _uiState.update { it.copy(savedRecipeTitles = it.savedRecipeTitles + recipe.title) }
            onSuccess()
        }
    }

    fun addRecipeToMealPlan(
        recipe: GeneratedRecipe,
        date: String,
        slot: MealSlot,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch(dispatcher) {
            val item = MealPlanItem(
                date = date,
                slot = slot,
                recipeTitle = recipe.title,
                ingredients = recipe.ingredients,
                isUserRecipe = false
            )
            mealPlanRepository.addMealPlanItem(item)
            onSuccess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            geminiAiRepository: GeminiAiRepository,
            aiPreferencesRepository: AiPreferencesRepository,
            userRecipeRepository: UserRecipeRepository,
            mealPlanRepository: MealPlanRepository,
            syncRepository: SyncRepository? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FridgeToTableViewModel(
                    geminiAiRepository = geminiAiRepository,
                    aiPreferencesRepository = aiPreferencesRepository,
                    userRecipeRepository = userRecipeRepository,
                    mealPlanRepository = mealPlanRepository,
                    syncRepository = syncRepository
                ) as T
            }
        }
    }
}
