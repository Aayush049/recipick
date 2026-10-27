package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.CommunityRecipe
import com.aayush.recipick.domain.model.CommunityTip
import com.aayush.recipick.domain.repository.CommunityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommunityDetailUiState(
    val recipe: CommunityRecipe? = null,
    val tips: List<CommunityTip> = emptyList(),
    val userRating: Int? = null,
    val isLoading: Boolean = true,
    val isSubmittingRating: Boolean = false,
    val isPostingTip: Boolean = false,
    val isSavingToCookbook: Boolean = false,
    val userMessage: String? = null
)

class CommunityDetailViewModel(
    private val recipeId: String,
    private val repository: CommunityRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    init {
        observeRecipe()
        observeTips()
        observeUserRating()
    }

    private fun observeRecipe() {
        viewModelScope.launch(dispatcher) {
            repository.getCommunityRecipeById(recipeId).collect { recipe ->
                _uiState.value = _uiState.value.copy(
                    recipe = recipe,
                    isLoading = false
                )
            }
        }
    }

    private fun observeTips() {
        viewModelScope.launch(dispatcher) {
            repository.getRecipeTips(recipeId).collect { tipsList ->
                _uiState.value = _uiState.value.copy(tips = tipsList)
            }
        }
    }

    private fun observeUserRating() {
        viewModelScope.launch(dispatcher) {
            repository.getUserRating(recipeId).collect { rating ->
                _uiState.value = _uiState.value.copy(userRating = rating)
            }
        }
    }

    fun submitRating(rating: Int, onComplete: (Boolean, String) -> Unit) {
        _uiState.value = _uiState.value.copy(isSubmittingRating = true)
        viewModelScope.launch(dispatcher) {
            val result = repository.rateRecipe(recipeId, rating)
            _uiState.value = _uiState.value.copy(isSubmittingRating = false)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(userRating = rating)
                    onComplete(true, "Thank you for rating!")
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Failed to submit rating"
                    onComplete(false, msg)
                }
            )
        }
    }

    fun postTip(tipText: String, authorName: String, onComplete: (Boolean, String) -> Unit) {
        if (tipText.isBlank()) {
            onComplete(false, "Tip cannot be empty.")
            return
        }
        _uiState.value = _uiState.value.copy(isPostingTip = true)
        viewModelScope.launch(dispatcher) {
            val result = repository.addRecipeTip(recipeId, tipText, authorName)
            _uiState.value = _uiState.value.copy(isPostingTip = false)
            result.fold(
                onSuccess = {
                    onComplete(true, "Tip added to community discussion!")
                },
                onFailure = { err ->
                    val msg = err.localizedMessage ?: "Failed to post tip"
                    onComplete(false, msg)
                }
            )
        }
    }

    fun saveToMyRecipes(onComplete: (Boolean, String) -> Unit) {
        val current = _uiState.value.recipe ?: return
        _uiState.value = _uiState.value.copy(isSavingToCookbook = true)
        viewModelScope.launch(dispatcher) {
            val result = repository.saveCommunityRecipeToPersonal(current)
            _uiState.value = _uiState.value.copy(isSavingToCookbook = false)
            result.fold(
                onSuccess = {
                    onComplete(true, "Recipe saved to My Recipes cookbook!")
                },
                onFailure = { err ->
                    onComplete(false, err.localizedMessage ?: "Failed to save recipe")
                }
            )
        }
    }

    companion object {
        fun provideFactory(
            recipeId: String,
            repository: CommunityRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CommunityDetailViewModel(recipeId, repository, dispatcher) as T
                }
            }
    }
}
