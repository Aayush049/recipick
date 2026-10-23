package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMyRecipesRepository : UserRecipeRepository {
    var recipesList = listOf(
        UserRecipe(
            id = 1,
            name = "Test Recipe",
            ingredients = listOf("Salt"),
            instructions = listOf("Cook"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            servings = 2,
            difficulty = "Easy",
            cuisine = "Italian"
        )
    )
    var deletedRecipe: UserRecipe? = null

    override fun getAllUserRecipes(): Flow<List<UserRecipe>> = flowOf(recipesList)

    override fun getUserRecipeById(id: Int): Flow<UserRecipe?> =
        flowOf(recipesList.find { it.id == id })

    override suspend fun insertUserRecipe(recipe: UserRecipe): Long = 1L

    override suspend fun updateUserRecipe(recipe: UserRecipe) {}

    override suspend fun deleteUserRecipe(recipe: UserRecipe) {
        deletedRecipe = recipe
        recipesList = recipesList.filter { it.id != recipe.id }
    }

    override suspend fun deleteUserRecipeById(id: Int) {
        recipesList = recipesList.filter { it.id != id }
    }
}

class MyRecipesViewModelTest {

    @Test
    fun testPopulatedState() {
        val repo = FakeMyRecipesRepository()
        val viewModel = MyRecipesViewModel(repo, Dispatchers.Unconfined)
        val state = viewModel.uiState.value

        assertEquals(1, state.recipes.size)
        assertEquals("Test Recipe", state.recipes[0].name)
    }

    @Test
    fun testEmptyState() {
        val repo = FakeMyRecipesRepository()
        repo.recipesList = emptyList()
        val viewModel = MyRecipesViewModel(repo, Dispatchers.Unconfined)
        val state = viewModel.uiState.value

        assertTrue(state.recipes.isEmpty())
    }

    @Test
    fun testDeleteRecipe() {
        val repo = FakeMyRecipesRepository()
        val viewModel = MyRecipesViewModel(repo, Dispatchers.Unconfined)
        val recipeToDelete = repo.recipesList[0]

        viewModel.deleteRecipe(recipeToDelete)

        assertEquals(recipeToDelete, repo.deletedRecipe)
    }
}
