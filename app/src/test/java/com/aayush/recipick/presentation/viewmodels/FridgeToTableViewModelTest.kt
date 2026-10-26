package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.repository.FakeMealPlanDao
import com.aayush.recipick.data.repository.FakeUserRecipeDao
import com.aayush.recipick.data.repository.MealPlanRepositoryImpl
import com.aayush.recipick.data.repository.UserRecipeRepositoryImpl
import com.aayush.recipick.domain.model.GeneratedRecipe
import com.aayush.recipick.domain.model.MealSlot
import com.aayush.recipick.domain.repository.AiPreferencesRepository
import com.aayush.recipick.domain.repository.GeminiAiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FakeGeminiAiRepository(
    var shouldSucceed: Boolean = true,
    var mockRecipes: List<GeneratedRecipe> = listOf(
        GeneratedRecipe(
            title = "Quick Egg Fried Rice",
            cuisine = "Asian",
            difficulty = "Easy",
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            servings = 2,
            description = "A classic stir-fried rice using leftover cold rice, eggs, and scallions.",
            ingredients = listOf("2 cups cooked rice", "2 eggs", "2 tbsp soy sauce", "1 green onion"),
            instructions = listOf("Beat the eggs.", "Stir fry eggs and set aside.", "Stir fry rice and mix together.")
        )
    )
) : GeminiAiRepository {
    override suspend fun generateFridgeToTableRecipes(
        ingredients: List<String>,
        dietaryPreferences: List<String>,
        mealType: String
    ): Result<List<GeneratedRecipe>> {
        return if (shouldSucceed) {
            Result.success(mockRecipes)
        } else {
            Result.failure(RuntimeException("Gemini API key is invalid or rate limited."))
        }
    }
}

class FakeAiPreferencesRepository(var key: String? = "valid-api-key") : AiPreferencesRepository {
    override fun getCustomApiKey(): Flow<String?> = flowOf(key)
    override suspend fun setCustomApiKey(key: String?) { this.key = key }
    override suspend fun getEffectiveApiKey(): String = key ?: ""
}

@OptIn(ExperimentalCoroutinesApi::class)
class FridgeToTableViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAiRepo: FakeGeminiAiRepository
    private lateinit var fakeAiPrefRepo: FakeAiPreferencesRepository
    private lateinit var userRecipeDao: FakeUserRecipeDao
    private lateinit var mealPlanDao: FakeMealPlanDao
    private lateinit var userRecipeRepo: UserRecipeRepositoryImpl
    private lateinit var mealPlanRepo: MealPlanRepositoryImpl
    private lateinit var viewModel: FridgeToTableViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAiRepo = FakeGeminiAiRepository()
        fakeAiPrefRepo = FakeAiPreferencesRepository()
        userRecipeDao = FakeUserRecipeDao()
        mealPlanDao = FakeMealPlanDao()
        userRecipeRepo = UserRecipeRepositoryImpl(userRecipeDao)
        mealPlanRepo = MealPlanRepositoryImpl(mealPlanDao)

        viewModel = FridgeToTableViewModel(
            geminiAiRepository = fakeAiRepo,
            aiPreferencesRepository = fakeAiPrefRepo,
            userRecipeRepository = userRecipeRepo,
            mealPlanRepository = mealPlanRepo,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addAndRemoveIngredient_updatesUiStateCorrectly() = runTest {
        viewModel.addIngredient("Eggs")
        viewModel.addIngredient("Tomatoes")
        viewModel.addIngredient("eggs") // Duplicate case check

        val state = viewModel.uiState.value
        assertEquals(2, state.ingredients.size)
        assertTrue(state.ingredients.contains("Eggs"))
        assertTrue(state.ingredients.contains("Tomatoes"))

        viewModel.removeIngredient("Eggs")
        assertEquals(1, viewModel.uiState.value.ingredients.size)
        assertFalse(viewModel.uiState.value.ingredients.contains("Eggs"))
    }

    @Test
    fun toggleDietary_togglesSelection() = runTest {
        viewModel.toggleDietary("Vegetarian")
        assertTrue(viewModel.uiState.value.dietaryPreferences.contains("Vegetarian"))

        viewModel.toggleDietary("Vegetarian")
        assertFalse(viewModel.uiState.value.dietaryPreferences.contains("Vegetarian"))
    }

    @Test
    fun selectMealType_updatesSelection() = runTest {
        viewModel.selectMealType("Dinner")
        assertEquals("Dinner", viewModel.uiState.value.selectedMealType)
    }

    @Test
    fun generateRecipes_success_populatesGeneratedRecipes() = runTest {
        viewModel.addIngredient("Eggs")
        viewModel.addIngredient("Rice")

        viewModel.generateRecipes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertNull(state.error)
        assertEquals(1, state.generatedRecipes.size)
        assertEquals("Quick Egg Fried Rice", state.generatedRecipes.first().title)
    }

    @Test
    fun generateRecipes_failure_setsErrorMessage() = runTest {
        fakeAiRepo.shouldSucceed = false
        viewModel.addIngredient("Eggs")

        viewModel.generateRecipes()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Gemini API key"))
    }

    @Test
    fun saveRecipeToPersonal_insertsIntoCookbook() = runTest {
        val recipe = fakeAiRepo.mockRecipes.first()

        viewModel.saveRecipeToMyCookbook(recipe)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.savedRecipeTitles.contains(recipe.title))
        val inDb = userRecipeDao.getAllRecipesList()
        assertEquals(1, inDb.size)
        assertEquals(recipe.title, inDb.first().name)
    }

    @Test
    fun addRecipeToMealPlan_insertsMealPlanItem() = runTest {
        val recipe = fakeAiRepo.mockRecipes.first()
        val date = LocalDate.of(2026, 10, 27).toString()
        var callbackCalled = false

        viewModel.addRecipeToMealPlan(recipe, date, MealSlot.DINNER) {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        val items = mealPlanDao.itemsFlow.value
        assertEquals(1, items.size)
        assertEquals(recipe.title, items.first().recipeTitle)
        assertEquals(MealSlot.DINNER, items.first().slot)
    }
}
