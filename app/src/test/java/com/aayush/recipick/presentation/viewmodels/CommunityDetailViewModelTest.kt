package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.CommunityRecipe
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeCommunityRepository
    private lateinit var viewModel: CommunityDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCommunityRepository()

        fakeRepo.recipesFlow.value = listOf(
            CommunityRecipe(
                id = "recipe_42",
                name = "Homemade Margherita Pizza",
                cuisine = "Italian",
                authorName = "Chef Luigi",
                ingredients = listOf("Pizza dough", "Tomato sauce", "Mozzarella", "Basil"),
                instructions = listOf("Roll dough", "Spread sauce & cheese", "Bake at 450F for 12m"),
                averageRating = 4.5,
                ratingCount = 10,
                tipCount = 0
            )
        )

        viewModel = CommunityDetailViewModel("recipe_42", fakeRepo, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_observesRecipeAndInitialState() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.recipe)
        assertEquals("Homemade Margherita Pizza", state.recipe?.name)
    }

    @Test
    fun submitRating_updatesUserRatingAndSubmitsToRepo() = runTest {
        advanceUntilIdle()

        var callbackCalled = false
        viewModel.submitRating(5) { success, _ ->
            callbackCalled = true
            assertTrue(success)
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals(5, viewModel.uiState.value.userRating)
        assertEquals(5, fakeRepo.ratingsMap["recipe_42"])
    }

    @Test
    fun postTip_addsTipToRecipe() = runTest {
        advanceUntilIdle()

        var callbackCalled = false
        viewModel.postTip("Add fresh basil AFTER baking to keep it aromatic!", "Pizza Lover") { success, _ ->
            callbackCalled = true
            assertTrue(success)
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertEquals(1, viewModel.uiState.value.tips.size)
        assertEquals("Add fresh basil AFTER baking to keep it aromatic!", viewModel.uiState.value.tips.first().tipText)
    }

    @Test
    fun saveToMyRecipes_clonesRecipe() = runTest {
        advanceUntilIdle()

        var callbackCalled = false
        viewModel.saveToMyRecipes { success, _ ->
            callbackCalled = true
            assertTrue(success)
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertFalse(viewModel.uiState.value.isSavingToCookbook)
    }
}
