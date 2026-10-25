package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.domain.repository.RecipeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun fetchRecipeDetails_success_updatesRecipeAndEmitsSuccessState() = runTest {
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
        assertTrue(viewModel.uiState.value is RecipeDetailUiState.Loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RecipeDetailUiState.Success)
        assertEquals("Classic Margherita Pizza", (state as RecipeDetailUiState.Success).recipe.name)
    }

    @Test
    fun fetchRecipeDetails_noInternet_emitsRetryableError() = runTest {
        val repo = FakeRecipeRepository(exceptionToThrow = UnknownHostException("Unable to resolve host"))
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RecipeDetailUiState.Error.Retryable)
        assertEquals(
            "No internet connection. Please check your network and try again.",
            (state as RecipeDetailUiState.Error.Retryable).message
        )
    }

    @Test
    fun fetchRecipeDetails_timeout_emitsRetryableError() = runTest {
        val repo = FakeRecipeRepository(exceptionToThrow = SocketTimeoutException("connect timed out"))
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RecipeDetailUiState.Error.Retryable)
        assertEquals(
            "Connection timed out. The server took too long to respond.",
            (state as RecipeDetailUiState.Error.Retryable).message
        )
    }

    @Test
    fun fetchRecipeDetails_notFound404_emitsNonRetryableError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"message": "Recipe not found"}"""),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) { expectSuccess = true }
        var notFoundException: ClientRequestException? = null
        try {
            client.get("https://dummyjson.com/recipes/9999").body<String>()
        } catch (e: ClientRequestException) {
            notFoundException = e
        }

        val repo = FakeRecipeRepository(exceptionToThrow = notFoundException!!)
        val viewModel = RecipeDetailsViewModel(repository = repo)

        viewModel.fetchRecipeDetails(9999)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RecipeDetailUiState.Error.NonRetryable)
        assertEquals(
            "Recipe not found.",
            (state as RecipeDetailUiState.Error.NonRetryable).message
        )
    }
}
