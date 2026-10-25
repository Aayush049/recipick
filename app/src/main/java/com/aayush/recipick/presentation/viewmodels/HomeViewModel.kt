package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.data.remote.KtorClient
import com.aayush.recipick.data.remote.RecipeAPIService
import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.data.repository.RecipeRepositoryImplementation
import com.aayush.recipick.domain.repository.RecipeRepository
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

class HomeViewModel(
    private val repository: RecipeRepository = RecipeRepositoryImplementation(
        apiService = RecipeAPIService(client = KtorClient.client)
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allRecipes: List<Recipe> = emptyList()
    private var allCategories: List<String> = listOf("All")
    private var currentCategory: String = "All"

    init {
        fetchRecipes()
    }

    fun fetchRecipes() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.getAllRecipes()
                allRecipes = result

                val cuisines = result.map { it.cuisine }.distinct().sorted()
                allCategories = listOf("All") + cuisines
                currentCategory = "All"

                _uiState.value = HomeUiState.Success(
                    recipes = result,
                    categories = allCategories,
                    selectedCategory = currentCategory
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = HomeUiState.Error(mapErrorMessage(e))
            }
        }
    }

    fun onCategorySelected(category: String) {
        currentCategory = category
        val filtered = if (category == "All") {
            allRecipes
        } else {
            allRecipes.filter { it.cuisine.equals(category, ignoreCase = true) }
        }
        _uiState.value = HomeUiState.Success(
            recipes = filtered,
            categories = allCategories,
            selectedCategory = currentCategory
        )
    }

    private fun mapErrorMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException,
            is ConnectException,
            is UnresolvedAddressException -> {
                "No internet connection. Please check your network and try again."
            }
            is HttpRequestTimeoutException,
            is SocketTimeoutException -> {
                "Connection timed out. The server took too long to respond."
            }
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
