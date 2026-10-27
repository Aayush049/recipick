package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.CommunityRecipe
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.CommunityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CommunitySortOption(val displayName: String) {
    NEWEST("Latest"),
    HIGHEST_RATED("Top Rated"),
    MOST_TIPS("Most Discussed")
}

data class CommunityUiState(
    val recipes: List<CommunityRecipe> = emptyList(),
    val searchQuery: String = "",
    val sortOption: CommunitySortOption = CommunitySortOption.NEWEST,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class CommunityViewModel(
    private val repository: CommunityRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _rawRecipes = MutableStateFlow<List<CommunityRecipe>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(CommunitySortOption.NEWEST)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CommunityUiState> = combine(
        _rawRecipes,
        _searchQuery,
        _sortOption,
        _isLoading,
        _errorMessage
    ) { raw, query, sort, loading, error ->
        val filtered = if (query.isBlank()) {
            raw
        } else {
            val q = query.trim().lowercase()
            raw.filter {
                it.name.lowercase().contains(q) ||
                        it.cuisine.lowercase().contains(q) ||
                        it.authorName.lowercase().contains(q) ||
                        it.ingredients.any { ing -> ing.lowercase().contains(q) }
            }
        }

        val sorted = when (sort) {
            CommunitySortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
            CommunitySortOption.HIGHEST_RATED -> filtered.sortedWith(
                compareByDescending<CommunityRecipe> { it.averageRating }
                    .thenByDescending { it.ratingCount }
            )
            CommunitySortOption.MOST_TIPS -> filtered.sortedByDescending { it.tipCount }
        }

        CommunityUiState(
            recipes = sorted,
            searchQuery = query,
            sortOption = sort,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CommunityUiState()
    )

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        _isLoading.value = true
        viewModelScope.launch(dispatcher) {
            try {
                repository.getCommunityRecipes().collect { list ->
                    _rawRecipes.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.localizedMessage ?: "Failed to load community recipes"
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChanged(option: CommunitySortOption) {
        _sortOption.value = option
    }

    fun shareRecipe(
        userRecipe: UserRecipe,
        authorName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            val result = repository.shareRecipeToCommunity(userRecipe, authorName)
            result.fold(
                onSuccess = { recipeId -> onSuccess(recipeId) },
                onFailure = { err -> onError(err.localizedMessage ?: "Failed to share recipe") }
            )
        }
    }

    fun rateRecipe(
        recipeId: String,
        rating: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            val result = repository.rateRecipe(recipeId, rating)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err -> onError(err.localizedMessage ?: "Failed to rate recipe") }
            )
        }
    }

    fun saveCommunityRecipe(
        recipe: CommunityRecipe,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(dispatcher) {
            val result = repository.saveCommunityRecipeToPersonal(recipe)
            result.fold(
                onSuccess = { localId -> onSuccess(localId) },
                onFailure = { err -> onError(err.localizedMessage ?: "Failed to save recipe") }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun provideFactory(
            repository: CommunityRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CommunityViewModel(repository, dispatcher) as T
                }
            }
    }
}
