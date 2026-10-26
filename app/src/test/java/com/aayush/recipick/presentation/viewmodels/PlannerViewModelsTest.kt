package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.repository.FakeGroceryDao
import com.aayush.recipick.data.repository.FakeMealPlanDao
import com.aayush.recipick.data.repository.GroceryRepositoryImpl
import com.aayush.recipick.data.repository.MealPlanRepositoryImpl
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.model.MealSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mealPlanDao: FakeMealPlanDao
    private lateinit var groceryDao: FakeGroceryDao
    private lateinit var mealPlanRepository: MealPlanRepositoryImpl
    private lateinit var groceryRepository: GroceryRepositoryImpl
    private lateinit var mealPlanViewModel: MealPlanViewModel
    private lateinit var groceryViewModel: GroceryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mealPlanDao = FakeMealPlanDao()
        groceryDao = FakeGroceryDao()
        mealPlanRepository = MealPlanRepositoryImpl(mealPlanDao)
        groceryRepository = GroceryRepositoryImpl(groceryDao)

        mealPlanViewModel = MealPlanViewModel(
            mealPlanRepository = mealPlanRepository,
            groceryRepository = groceryRepository,
            dispatcher = testDispatcher
        )

        groceryViewModel = GroceryViewModel(
            repository = groceryRepository,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `MealPlanViewModel initializes with current week and 7 days`() = runTest {
        advanceUntilIdle()
        val state = mealPlanViewModel.uiState.value
        assertEquals(7, state.weekDays.size)
        assertTrue(state.isCurrentWeek)
        assertEquals(LocalDate.now(), state.selectedDate)
    }

    @Test
    fun `MealPlanViewModel navigates between weeks`() = runTest {
        advanceUntilIdle()
        val initialStart = mealPlanViewModel.uiState.value.weekStartDate

        mealPlanViewModel.nextWeek()
        advanceUntilIdle()
        assertEquals(initialStart.plusWeeks(1), mealPlanViewModel.uiState.value.weekStartDate)

        mealPlanViewModel.previousWeek()
        advanceUntilIdle()
        assertEquals(initialStart, mealPlanViewModel.uiState.value.weekStartDate)
    }

    @Test
    fun `MealPlanViewModel adds and removes meals`() = runTest {
        advanceUntilIdle()

        mealPlanViewModel.addMeal(
            slot = MealSlot.BREAKFAST,
            recipeId = 10,
            title = "Avocado Toast",
            imageUrl = null,
            isUserRecipe = true,
            customNote = null,
            ingredients = listOf("Bread", "Avocado", "Salt")
        )
        advanceUntilIdle()

        val state = mealPlanViewModel.uiState.value
        assertEquals(1, state.weekMealPlans.size)
        assertNotNull(state.selectedDateMealPlans[MealSlot.BREAKFAST])
        assertEquals("Avocado Toast", state.selectedDateMealPlans[MealSlot.BREAKFAST]?.recipeTitle)

        val mealId = state.weekMealPlans[0].id
        mealPlanViewModel.removeMeal(mealId)
        advanceUntilIdle()

        assertEquals(0, mealPlanViewModel.uiState.value.weekMealPlans.size)
    }

    @Test
    fun `GroceryViewModel adds custom items and toggles checked`() = runTest {
        advanceUntilIdle()

        groceryViewModel.addItem("Bananas", GroceryCategory.PRODUCE)
        advanceUntilIdle()

        var state = groceryViewModel.uiState.value
        assertEquals(1, state.totalCount)
        assertEquals(0, state.checkedCount)
        assertEquals("Bananas", state.items[0].name)

        groceryViewModel.toggleChecked(state.items[0])
        advanceUntilIdle()

        state = groceryViewModel.uiState.value
        assertEquals(1, state.checkedCount)
        assertTrue(state.items[0].isChecked)

        groceryViewModel.clearChecked()
        advanceUntilIdle()

        state = groceryViewModel.uiState.value
        assertEquals(0, state.totalCount)
    }
}
