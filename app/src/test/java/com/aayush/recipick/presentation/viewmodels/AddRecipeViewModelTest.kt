package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeUserRecipeRepository : UserRecipeRepository {
    val insertedRecipes = mutableListOf<UserRecipe>()

    override fun getAllUserRecipes(): Flow<List<UserRecipe>> = flowOf(insertedRecipes)

    override fun getUserRecipeById(id: Int): Flow<UserRecipe?> = flowOf(null)

    override suspend fun insertUserRecipe(recipe: UserRecipe): Long {
        insertedRecipes.add(recipe)
        return insertedRecipes.size.toLong()
    }

    override suspend fun updateUserRecipe(recipe: UserRecipe) {}

    override suspend fun deleteUserRecipe(recipe: UserRecipe) {}

    override suspend fun deleteUserRecipeById(id: Int) {}
}

class AddRecipeViewModelTest {

    @Test
    fun testInitialState() {
        val repo = FakeUserRecipeRepository()
        val viewModel = AddRecipeViewModel(repo, Dispatchers.Unconfined)
        val state = viewModel.uiState.value

        assertEquals("", state.name)
        assertEquals(emptyList<String>(), state.ingredients)
        assertEquals(emptyList<String>(), state.instructions)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun testValidationFailureWhenNameBlank() {
        val repo = FakeUserRecipeRepository()
        val viewModel = AddRecipeViewModel(repo, Dispatchers.Unconfined)

        viewModel.addIngredient("Flour")
        viewModel.addInstruction("Mix")
        viewModel.saveRecipe()

        val state = viewModel.uiState.value
        assertNotNull(state.nameError)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun testValidationFailureWhenIngredientsEmpty() {
        val repo = FakeUserRecipeRepository()
        val viewModel = AddRecipeViewModel(repo, Dispatchers.Unconfined)

        viewModel.updateName("Cake")
        viewModel.addInstruction("Bake")
        viewModel.saveRecipe()

        val state = viewModel.uiState.value
        assertNotNull(state.ingredientsError)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun testValidationFailureWhenInstructionsEmpty() {
        val repo = FakeUserRecipeRepository()
        val viewModel = AddRecipeViewModel(repo, Dispatchers.Unconfined)

        viewModel.updateName("Cake")
        viewModel.addIngredient("Flour")
        viewModel.saveRecipe()

        val state = viewModel.uiState.value
        assertNotNull(state.instructionsError)
        assertEquals(false, state.isSaved)
    }

    @Test
    fun testSuccessfulSave() {
        val repo = FakeUserRecipeRepository()
        val viewModel = AddRecipeViewModel(repo, Dispatchers.Unconfined)

        viewModel.updateName("Pancakes")
        viewModel.addIngredient("Flour")
        viewModel.addInstruction("Cook")
        viewModel.saveRecipe()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertEquals(1, repo.insertedRecipes.size)
        assertEquals("Pancakes", repo.insertedRecipes[0].name)
    }
}
