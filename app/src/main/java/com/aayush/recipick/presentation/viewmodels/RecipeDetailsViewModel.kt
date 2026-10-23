package com.aayush.recipick.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.data.remote.KtorClient
import com.aayush.recipick.data.remote.RecipeAPIService
import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.data.repository.RecipeRepositoryImplementation
import com.aayush.recipick.domain.repository.RecipeRepository
import kotlinx.coroutines.launch

class RecipeDetailsViewModel: ViewModel() {

    private val repository: RecipeRepository = RecipeRepositoryImplementation(
        apiService = RecipeAPIService(client = KtorClient.client)
    )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var recipe by mutableStateOf<Recipe?>(null)
        private set

    fun fetchRecipeDetails(recipeId: Int){
        isLoading = true
        errorMessage = null

        try {
            viewModelScope.launch {
                recipe = repository.getRecipeByID(recipeId)
            }
        }
        catch (e: Exception){
            errorMessage = e.message ?: "Unknown Error"
        }
        finally {
            isLoading = false
        }
    }
}