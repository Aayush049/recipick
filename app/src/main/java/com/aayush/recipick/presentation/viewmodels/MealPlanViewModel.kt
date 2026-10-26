package com.aayush.recipick.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.model.MealSlot
import com.aayush.recipick.domain.repository.GroceryRepository
import com.aayush.recipick.domain.repository.MealPlanRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class MealPlanUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val weekStartDate: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val weekDays: List<LocalDate> = emptyList(),
    val weekMealPlans: List<MealPlanItem> = emptyList(),
    val selectedDateMealPlans: Map<MealSlot, MealPlanItem> = emptyMap(),
    val isCurrentWeek: Boolean = true,
    val generatedGroceryCount: Int? = null,
    val isLoading: Boolean = false
)

class MealPlanViewModel(
    private val mealPlanRepository: MealPlanRepository,
    private val groceryRepository: GroceryRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private var mealPlanJob: Job? = null

    init {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        updateWeek(monday, today)
    }

    private fun updateWeek(weekStart: LocalDate, selectedDate: LocalDate) {
        val days = (0L..6L).map { weekStart.plusDays(it) }
        val today = LocalDate.now()
        val currentWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        _uiState.update {
            it.copy(
                weekStartDate = weekStart,
                selectedDate = selectedDate,
                weekDays = days,
                isCurrentWeek = weekStart == currentWeekMonday,
                isLoading = true
            )
        }

        observeWeekMeals(weekStart, days.last())
    }

    private fun observeWeekMeals(startDate: LocalDate, endDate: LocalDate) {
        mealPlanJob?.cancel()
        val startStr = startDate.format(dateFormatter)
        val endStr = endDate.format(dateFormatter)

        mealPlanJob = viewModelScope.launch(dispatcher) {
            mealPlanRepository.getMealPlanForWeek(startStr, endStr).collect { meals ->
                _uiState.update { current ->
                    val selectedStr = current.selectedDate.format(dateFormatter)
                    val dateMeals = meals.filter { it.date == selectedStr }.associateBy { it.slot }
                    current.copy(
                        weekMealPlans = meals,
                        selectedDateMealPlans = dateMeals,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val selectedStr = date.format(dateFormatter)
        _uiState.update { current ->
            val dateMeals = current.weekMealPlans.filter { it.date == selectedStr }.associateBy { it.slot }
            current.copy(
                selectedDate = date,
                selectedDateMealPlans = dateMeals
            )
        }
    }

    fun previousWeek() {
        val newStart = _uiState.value.weekStartDate.minusWeeks(1)
        updateWeek(newStart, newStart)
    }

    fun nextWeek() {
        val newStart = _uiState.value.weekStartDate.plusWeeks(1)
        updateWeek(newStart, newStart)
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        updateWeek(monday, today)
    }

    fun addMeal(
        slot: MealSlot,
        recipeId: Int?,
        title: String,
        imageUrl: String?,
        isUserRecipe: Boolean,
        customNote: String?,
        ingredients: List<String>
    ) {
        val dateStr = _uiState.value.selectedDate.format(dateFormatter)
        val item = MealPlanItem(
            date = dateStr,
            slot = slot,
            recipeId = recipeId,
            recipeTitle = title,
            recipeImageUrl = imageUrl,
            isUserRecipe = isUserRecipe,
            customNote = customNote,
            ingredients = ingredients
        )
        viewModelScope.launch(dispatcher) {
            mealPlanRepository.addMealPlanItem(item)
        }
    }

    fun removeMeal(id: Long) {
        viewModelScope.launch(dispatcher) {
            mealPlanRepository.removeMealPlanItem(id)
        }
    }

    fun generateGroceryList(onResult: (Int) -> Unit) {
        viewModelScope.launch(dispatcher) {
            val weekMeals = _uiState.value.weekMealPlans
            val count = groceryRepository.generateFromMealPlan(weekMeals)
            _uiState.update { it.copy(generatedGroceryCount = count) }
            onResult(count)
        }
    }

    fun resetGeneratedCount() {
        _uiState.update { it.copy(generatedGroceryCount = null) }
    }

    companion object {
        fun provideFactory(
            mealPlanRepository: MealPlanRepository,
            groceryRepository: GroceryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MealPlanViewModel(
                    mealPlanRepository = mealPlanRepository,
                    groceryRepository = groceryRepository
                ) as T
            }
        }
    }
}
