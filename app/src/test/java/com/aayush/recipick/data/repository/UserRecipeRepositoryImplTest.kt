package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.UserRecipeEntity
import com.aayush.recipick.domain.model.AuthUser
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeUserRecipeDao : UserRecipeDao {
    val recipesFlow = MutableStateFlow<List<UserRecipeEntity>>(emptyList())

    override fun getAllRecipes(): Flow<List<UserRecipeEntity>> = recipesFlow

    override suspend fun getAllRecipesList(): List<UserRecipeEntity> = recipesFlow.value

    override fun getRecipesByUser(userId: String?): Flow<List<UserRecipeEntity>> {
        return recipesFlow.map { list ->
            list.filter { it.userId == userId }
        }
    }

    override suspend fun getRecipesListByUser(userId: String?): List<UserRecipeEntity> {
        return recipesFlow.value.filter { it.userId == userId }
    }

    override suspend fun claimGuestRecipes(userId: String) {
        recipesFlow.value = recipesFlow.value.map {
            if (it.userId == null) it.copy(userId = userId) else it
        }
    }

    override fun getRecipeById(id: Int): Flow<UserRecipeEntity?> {
        return recipesFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun insertRecipe(recipe: UserRecipeEntity): Long {
        val newId = (recipesFlow.value.maxOfOrNull { it.id } ?: 0) + 1
        val entity = recipe.copy(id = if (recipe.id == 0) newId else recipe.id)
        recipesFlow.value = recipesFlow.value.filter { it.id != entity.id } + entity
        return entity.id.toLong()
    }

    override suspend fun insertRecipes(recipes: List<UserRecipeEntity>) {
        val current = recipesFlow.value.toMutableList()
        for (r in recipes) {
            current.removeAll { it.id == r.id }
            current.add(r)
        }
        recipesFlow.value = current
    }

    override suspend fun updateRecipe(recipe: UserRecipeEntity) {
        recipesFlow.value = recipesFlow.value.map { if (it.id == recipe.id) recipe else it }
    }

    override suspend fun deleteRecipe(recipe: UserRecipeEntity) {
        recipesFlow.value = recipesFlow.value.filter { it.id != recipe.id }
    }

    override suspend fun deleteRecipeById(id: Int) {
        recipesFlow.value = recipesFlow.value.filter { it.id != id }
    }
}

class TestAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun setUser(user: AuthUser?) {
        _currentUser.value = user
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        Result.success(AuthUser("uid", "email", "name"))

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        Result.success(AuthUser("uid", "email", "name"))

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String?): Result<AuthUser> =
        Result.success(AuthUser("uid", "email", displayName))

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }
}

class UserRecipeRepositoryImplTest {

    private lateinit var fakeDao: FakeUserRecipeDao
    private lateinit var testAuthRepo: TestAuthRepository
    private lateinit var repository: UserRecipeRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeUserRecipeDao()
        testAuthRepo = TestAuthRepository()
        repository = UserRecipeRepositoryImpl(
            userRecipeDao = fakeDao,
            authRepository = testAuthRepo
        )
    }

    @Test
    fun `guest recipes are scoped to null userId and invisible to logged in user`() = runTest {
        // Guest adds a recipe
        repository.insertUserRecipe(
            UserRecipe(
                name = "Guest Salad",
                ingredients = listOf("Lettuce"),
                instructions = listOf("Toss"),
                prepTimeMinutes = 5,
                cookTimeMinutes = 0,
                servings = 1,
                difficulty = "Easy",
                cuisine = "Global"
            )
        )

        // Guest sees the recipe
        val guestRecipes = repository.getAllUserRecipes().first()
        assertEquals(1, guestRecipes.size)
        assertEquals("Guest Salad", guestRecipes[0].name)
        assertEquals(null, guestRecipes[0].userId)

        // Account B signs in
        testAuthRepo.setUser(AuthUser(uid = "account_b", email = "b@gmail.com", displayName = "Account B"))

        // Account B does not see guest recipes
        val userBRecipes = repository.getAllUserRecipes().first()
        assertTrue(userBRecipes.isEmpty())
    }

    @Test
    fun `Account A recipes are strictly isolated from Account B on same device`() = runTest {
        // Account A logs in
        testAuthRepo.setUser(AuthUser(uid = "account_a", email = "a@gmail.com", displayName = "Account A"))

        // Account A inserts recipe
        repository.insertUserRecipe(
            UserRecipe(
                name = "Account A Pasta",
                ingredients = listOf("Pasta", "Tomato"),
                instructions = listOf("Boil", "Mix"),
                prepTimeMinutes = 10,
                cookTimeMinutes = 15,
                servings = 2,
                difficulty = "Medium",
                cuisine = "Italian"
            )
        )

        // Account A sees pasta
        val recipesA = repository.getAllUserRecipes().first()
        assertEquals(1, recipesA.size)
        assertEquals("Account A Pasta", recipesA[0].name)
        assertEquals("account_a", recipesA[0].userId)

        // Account A logs out and Account B logs in
        testAuthRepo.setUser(AuthUser(uid = "account_b", email = "b@gmail.com", displayName = "Account B"))

        // Account B sees NOTHING from Account A
        val recipesB = repository.getAllUserRecipes().first()
        assertEquals(0, recipesB.size)

        // Account B inserts their own recipe
        repository.insertUserRecipe(
            UserRecipe(
                name = "Account B Curry",
                ingredients = listOf("Chicken", "Curry paste"),
                instructions = listOf("Cook chicken", "Add paste"),
                prepTimeMinutes = 15,
                cookTimeMinutes = 30,
                servings = 4,
                difficulty = "Hard",
                cuisine = "Indian"
            )
        )

        // Account B sees only their curry
        val updatedRecipesB = repository.getAllUserRecipes().first()
        assertEquals(1, updatedRecipesB.size)
        assertEquals("Account B Curry", updatedRecipesB[0].name)
        assertEquals("account_b", updatedRecipesB[0].userId)

        // Switch back to Account A
        testAuthRepo.setUser(AuthUser(uid = "account_a", email = "a@gmail.com", displayName = "Account A"))

        // Account A sees ONLY their pasta
        val reloadedRecipesA = repository.getAllUserRecipes().first()
        assertEquals(1, reloadedRecipesA.size)
        assertEquals("Account A Pasta", reloadedRecipesA[0].name)
        assertEquals("account_a", reloadedRecipesA[0].userId)
    }

    @Test
    fun `claimGuestRecipes transfers unclaimed guest recipes to logged in user`() = runTest {
        // Guest creates a recipe
        repository.insertUserRecipe(
            UserRecipe(
                name = "Offline Bread",
                ingredients = listOf("Flour", "Yeast"),
                instructions = listOf("Knead", "Bake"),
                prepTimeMinutes = 20,
                cookTimeMinutes = 40,
                servings = 6,
                difficulty = "Medium",
                cuisine = "Bakery"
            )
        )

        assertEquals(null, fakeDao.getAllRecipesList()[0].userId)

        // User signs in and claims guest recipes
        fakeDao.claimGuestRecipes("account_a")
        testAuthRepo.setUser(AuthUser(uid = "account_a", email = "a@gmail.com", displayName = "Account A"))

        val recipesA = repository.getAllUserRecipes().first()
        assertEquals(1, recipesA.size)
        assertEquals("Offline Bread", recipesA[0].name)
        assertEquals("account_a", recipesA[0].userId)
    }

    @Test
    fun `deleteUserRecipe removes recipe from local database`() = runTest {
        val recipe = UserRecipe(
            id = 10,
            name = "Test Delete",
            ingredients = listOf("Item"),
            instructions = listOf("Step"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 5,
            servings = 1,
            difficulty = "Easy",
            cuisine = "Global"
        )
        repository.insertUserRecipe(recipe)
        assertEquals(1, fakeDao.recipesFlow.value.size)

        repository.deleteUserRecipe(recipe)
        assertEquals(0, fakeDao.recipesFlow.value.size)
    }

    @Test
    fun `deleteUserRecipeById removes recipe from local database by id`() = runTest {
        val recipe = UserRecipe(
            id = 20,
            name = "Test Delete By Id",
            ingredients = listOf("Item"),
            instructions = listOf("Step"),
            prepTimeMinutes = 5,
            cookTimeMinutes = 5,
            servings = 1,
            difficulty = "Easy",
            cuisine = "Global"
        )
        repository.insertUserRecipe(recipe)
        assertEquals(1, fakeDao.recipesFlow.value.size)

        repository.deleteUserRecipeById(20)
        assertEquals(0, fakeDao.recipesFlow.value.size)
    }
}
