package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.data.remote.KtorClient
import com.aayush.recipick.data.remote.RecipeAPIService
import com.aayush.recipick.data.repository.RecipeRepositoryImplementation
import com.aayush.recipick.domain.repository.RecipeRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    fun fetchRecipeDetails(recipeId: Int) {
        _uiState.value = RecipeDetailUiState.Loading

        viewModelScope.launch {
            try {
                val recipe = repository.getRecipeByID(recipeId)
                _uiState.value = RecipeDetailUiState.Success(recipe)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = mapErrorToUiState(e)
            }
        }
    }

    private fun mapErrorToUiState(e: Exception): RecipeDetailUiState.Error {
        return when (e) {
            is UnknownHostException,
            is ConnectException,
            is UnresolvedAddressException -> {
                RecipeDetailUiState.Error.Retryable("No internet connection. Please check your network and try again.")
            }
            is HttpRequestTimeoutException,
            is SocketTimeoutException -> {
                RecipeDetailUiState.Error.Retryable("Connection timed out. The server took too long to respond.")
            }
            is ClientRequestException -> {
                if (e.response.status == HttpStatusCode.NotFound) {
                    RecipeDetailUiState.Error.NonRetryable("Recipe not found.")
                } else {
                    RecipeDetailUiState.Error.Retryable("Request failed (${e.response.status.value}). Please try again.")
                }
            }
            else -> RecipeDetailUiState.Error.Retryable(e.message ?: "An unexpected error occurred. Please try again.")
        }
    }
}
