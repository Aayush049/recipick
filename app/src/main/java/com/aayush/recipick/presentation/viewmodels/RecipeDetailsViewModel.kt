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
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

class RecipeDetailsViewModel(
    private val repository: RecipeRepository = RecipeRepositoryImplementation(
        apiService = RecipeAPIService(client = KtorClient.client)
    )
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var recipe by mutableStateOf<Recipe?>(null)
        private set

    fun fetchRecipeDetails(recipeId: Int) {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                recipe = repository.getRecipeByID(recipeId)
            } catch (e: Exception) {
                errorMessage = mapErrorMessage(e)
            } finally {
                isLoading = false
            }
        }
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
            is ClientRequestException -> {
                if (e.response.status.value == 404) {
                    "Recipe not found."
                } else {
                    "Request failed (${e.response.status.value}). Please try again."
                }
            }
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
