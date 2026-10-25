package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class FakeRecipeRepository(
    var resultRecipe: Recipe? = null,
    var exceptionToThrow: Exception? = null
) : RecipeRepository {
    override suspend fun getAllRecipes(): List<Recipe> = emptyList()

    override suspend fun getRecipeByID(id: Int): Recipe {
        exceptionToThrow?.let { throw it }
        return resultRecipe ?: throw IllegalStateException("No recipe provided")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchRecipeDetails_success_updatesRecipeAndClearsLoading() = runTest {
        val sampleRecipe = Recipe(
            id = 1,
            name = "Classic Margherita Pizza",
            ingredients = listOf("Pizza dough", "Tomato sauce", "Fresh mozzarella"),
            instructions = listOf("Preheat oven", "Roll dough", "Bake"),
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = "Easy",
            cuisine = "Italian",
            caloriesPerServing = 300,
            tags = listOf("Pizza", "Italian"),
            userId = 1,
            image = "https://example.com/pizza.jpg",
            rating = 4.8,
            reviewCount = 50,
            mealType = listOf("Dinner")
        )
        val repo = FakeRecipeRepository(resultRecipe = sampleRecipe)
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(1)
        assertTrue(viewModel.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.errorMessage)
        assertNotNull(viewModel.recipe)
        assertEquals("Classic Margherita Pizza", viewModel.recipe?.name)
    }

    @Test
    fun fetchRecipeDetails_noInternet_setsFriendlyErrorMessage() = runTest {
        val repo = FakeRecipeRepository(exceptionToThrow = UnknownHostException("Unable to resolve host"))
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(1)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.recipe)
        assertEquals("No internet connection. Please check your network and try again.", viewModel.errorMessage)
    }

    @Test
    fun fetchRecipeDetails_timeout_setsTimeoutErrorMessage() = runTest {
        val repo = FakeRecipeRepository(exceptionToThrow = SocketTimeoutException("connect timed out"))
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(1)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading)
        assertNull(viewModel.recipe)
        assertEquals("Connection timed out. The server took too long to respond.", viewModel.errorMessage)
    }
}
