package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.GroceryItem
import com.aayush.recipick.domain.repository.GroceryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroceryUiState(
    val items: List<GroceryItem> = emptyList(),
    val groupedItems: Map<GroceryCategory, List<GroceryItem>> = emptyMap(),
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
    val isLoading: Boolean = false
)

class GroceryViewModel(
    private val repository: GroceryRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroceryUiState(isLoading = true))
    val uiState: StateFlow<GroceryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            repository.getAllGroceryItems().collect { items ->
                val grouped = items.groupBy { it.category }
                _uiState.update {
                    it.copy(
                        items = items,
                        groupedItems = grouped,
                        totalCount = items.size,
                        checkedCount = items.count { item -> item.isChecked },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleChecked(item: GroceryItem) {
        viewModelScope.launch(dispatcher) {
            repository.updateGroceryItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun addItem(name: String, category: GroceryCategory) {
        if (name.isBlank()) return
        viewModelScope.launch(dispatcher) {
            repository.addGroceryItem(
                GroceryItem(
                    name = name.trim(),
                    category = category
                )
            )
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch(dispatcher) {
            repository.deleteGroceryItem(id)
        }
    }

    fun clearChecked() {
        viewModelScope.launch(dispatcher) {
            repository.clearCheckedItems()
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatcher) {
            repository.clearAllItems()
        }
    }

    companion object {
        fun provideFactory(repository: GroceryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GroceryViewModel(repository) as T
                }
            }
    }
}
