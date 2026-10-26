package com.aayush.recipick.presentation.viewmodels

import com.aayush.recipick.domain.model.CommunityRecipe
import com.aayush.recipick.domain.model.CommunityTip
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

class FakeCommunityRepository : CommunityRepository {
    val recipesFlow = MutableStateFlow<List<CommunityRecipe>>(emptyList())
    val tipsMap = mutableMapOf<String, MutableStateFlow<List<CommunityTip>>>()
    val ratingsMap = mutableMapOf<String, Int>()

    override fun getCommunityRecipes(): Flow<List<CommunityRecipe>> = recipesFlow.asStateFlow()

    override fun getCommunityRecipeById(recipeId: String): Flow<CommunityRecipe?> =
        recipesFlow.map { list -> list.find { it.id == recipeId } }

    override suspend fun shareRecipeToCommunity(userRecipe: UserRecipe, authorName: String): Result<String> {
        val newId = "recipe_${recipesFlow.value.size + 1}"
        val newRecipe = CommunityRecipe(
            id = newId,
            authorId = "user_123",
            authorName = authorName.ifBlank { "Test Chef" },
            name = userRecipe.name,
            ingredients = userRecipe.ingredients,
            instructions = userRecipe.instructions,
            cuisine = userRecipe.cuisine,
            prepTimeMinutes = userRecipe.prepTimeMinutes,
            cookTimeMinutes = userRecipe.cookTimeMinutes,
            createdAt = System.currentTimeMillis()
        )
        recipesFlow.value = listOf(newRecipe) + recipesFlow.value
        return Result.success(newId)
    }

    override suspend fun rateRecipe(recipeId: String, rating: Int): Result<Unit> {
        ratingsMap[recipeId] = rating
        val currentList = recipesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            val r = currentList[index]
            val newCount = r.ratingCount + 1
            val newAvg = ((r.averageRating * r.ratingCount) + rating) / newCount
            currentList[index] = r.copy(averageRating = newAvg, ratingCount = newCount)
            recipesFlow.value = currentList
        }
        return Result.success(Unit)
    }

    override fun getUserRating(recipeId: String): Flow<Int?> = flowOf(ratingsMap[recipeId])

    override fun getRecipeTips(recipeId: String): Flow<List<CommunityTip>> =
        tipsMap.getOrPut(recipeId) { MutableStateFlow(emptyList()) }.asStateFlow()

    override suspend fun addRecipeTip(recipeId: String, tipText: String, authorName: String): Result<String> {
        val tipsFlow = tipsMap.getOrPut(recipeId) { MutableStateFlow(emptyList()) }
        val tipId = "tip_${tipsFlow.value.size + 1}"
        val tip = CommunityTip(
            id = tipId,
            recipeId = recipeId,
            authorId = "user_123",
            authorName = authorName,
            tipText = tipText
        )
        tipsFlow.value = listOf(tip) + tipsFlow.value

        val currentList = recipesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == recipeId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(tipCount = currentList[index].tipCount + 1)
            recipesFlow.value = currentList
        }
        return Result.success(tipId)
    }

    override suspend fun saveCommunityRecipeToPersonal(communityRecipe: CommunityRecipe): Result<Long> {
        return Result.success(999L)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeCommunityRepository
    private lateinit var viewModel: CommunityViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCommunityRepository()

        // Populate mock community data
        fakeRepo.recipesFlow.value = listOf(
            CommunityRecipe(
                id = "1",
                name = "Spicy Tofu Curry",
                cuisine = "Indian",
                authorName = "Chef Priya",
                ingredients = listOf("Tofu", "Curry powder", "Coconut milk"),
                averageRating = 4.9,
                ratingCount = 20,
                tipCount = 5,
                createdAt = 1000L
            ),
            CommunityRecipe(
                id = "2",
                name = "Italian Garlic Pasta",
                cuisine = "Italian",
                authorName = "Chef Marco",
                ingredients = listOf("Pasta", "Garlic", "Olive oil"),
                averageRating = 4.2,
                ratingCount = 8,
                tipCount = 2,
                createdAt = 2000L
            )
        )

        viewModel = CommunityViewModel(fakeRepo, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadRecipes_emitsCommunityRecipes() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.recipes.size)
    }

    @Test
    fun searchQuery_filtersByNameCuisineOrAuthor() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Pasta")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recipes.size)
        assertEquals("Italian Garlic Pasta", viewModel.uiState.value.recipes.first().name)

        viewModel.onSearchQueryChanged("Priya")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recipes.size)
        assertEquals("Spicy Tofu Curry", viewModel.uiState.value.recipes.first().name)

        viewModel.onSearchQueryChanged("Coconut milk")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recipes.size)
        assertEquals("Spicy Tofu Curry", viewModel.uiState.value.recipes.first().name)
    }

    @Test
    fun sortOption_sortsCorrectly() = runTest {
        advanceUntilIdle()

        // Default: NEWEST (id 2 has createdAt 2000L, id 1 has 1000L)
        viewModel.onSortOptionChanged(CommunitySortOption.NEWEST)
        advanceUntilIdle()
        assertEquals("Italian Garlic Pasta", viewModel.uiState.value.recipes.first().name)

        // HIGHEST_RATED (id 1 has 4.9, id 2 has 4.2)
        viewModel.onSortOptionChanged(CommunitySortOption.HIGHEST_RATED)
        advanceUntilIdle()
        assertEquals("Spicy Tofu Curry", viewModel.uiState.value.recipes.first().name)

        // MOST_TIPS (id 1 has 5 tips, id 2 has 2 tips)
        viewModel.onSortOptionChanged(CommunitySortOption.MOST_TIPS)
        advanceUntilIdle()
        assertEquals("Spicy Tofu Curry", viewModel.uiState.value.recipes.first().name)
    }

    @Test
    fun shareRecipe_addsNewCommunityRecipe() = runTest {
        advanceUntilIdle()

        val userRecipe = UserRecipe(
            name = "Avocado Toast",
            ingredients = listOf("Bread", "Avocado"),
            instructions = listOf("Toast bread", "Mash avocado"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 2,
            servings = 1,
            difficulty = "Easy",
            cuisine = "Breakfast"
        )

        var createdId: String? = null
        viewModel.shareRecipe(userRecipe, "Chef Home", onSuccess = { createdId = it }, onError = {})
        advanceUntilIdle()

        assertNotNull(createdId)
        val updated = fakeRepo.recipesFlow.value
        assertEquals(3, updated.size)
        assertEquals("Avocado Toast", updated.first().name)
    }

    @Test
    fun saveCommunityRecipe_callsPersonalCookbookRepository() = runTest {
        advanceUntilIdle()
        val recipe = fakeRepo.recipesFlow.value.first()

        var savedLocalId: Long? = null
        viewModel.saveCommunityRecipe(recipe, onSuccess = { savedLocalId = it }, onError = {})
        advanceUntilIdle()

        assertEquals(999L, savedLocalId)
    }
}
