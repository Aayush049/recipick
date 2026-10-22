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

class HomeViewModel: ViewModel() {

    private val repository : RecipeRepository = RecipeRepositoryImplementation(
        apiService = RecipeAPIService(client = KtorClient.client)
    )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    var categories by mutableStateOf<List<String>>(listOf("All"))
        private set

    var selectedCategory by mutableStateOf("All")
        private set

    private var allRecipes: List<Recipe> = emptyList()

    init {
        fetchRecipes()
    }

    fun fetchRecipes(){
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = repository.getAllRecipes()
                allRecipes = result

                val cuisines = result.map { it.cuisine }.distinct().sorted()
                categories = listOf("All") + cuisines

                applyFilters()
            }
            catch (e : Exception){
                errorMessage = e.message ?: "Unknown Error"
            }
            finally {
                isLoading = false
            }
        }
    }

    fun onCategorySelected(category: String){
        selectedCategory = category
        applyFilters()
    }

    private fun applyFilters(){
        recipes = if (selectedCategory == "All"){
            allRecipes
        }
        else {
            allRecipes.filter { it.cuisine == selectedCategory }
        }
    }
}