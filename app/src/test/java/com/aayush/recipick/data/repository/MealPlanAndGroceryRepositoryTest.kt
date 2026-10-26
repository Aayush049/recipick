package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.GroceryDao
import com.aayush.recipick.data.local.dao.MealPlanDao
import com.aayush.recipick.data.local.entity.GroceryItemEntity
import com.aayush.recipick.data.local.entity.MealPlanEntity
import com.aayush.recipick.domain.model.AuthUser
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.model.MealSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeMealPlanDao : MealPlanDao {
    val itemsFlow = MutableStateFlow<List<MealPlanEntity>>(emptyList())

    override fun getMealPlanForWeek(userId: String?, startDate: String, endDate: String): Flow<List<MealPlanEntity>> {
        return itemsFlow.map { list ->
            list.filter { it.userId == userId && it.date in startDate..endDate }
        }
    }

    override suspend fun insertMealPlan(item: MealPlanEntity): Long {
        val newId = (itemsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val entity = item.copy(id = if (item.id == 0L) newId else item.id)
        itemsFlow.value = itemsFlow.value.filter { it.id != entity.id } + entity
        return entity.id
    }

    override suspend fun deleteMealPlanById(id: Long) {
        itemsFlow.value = itemsFlow.value.filter { it.id != id }
    }

    override suspend fun clearMealPlanForDate(userId: String?, date: String) {
        itemsFlow.value = itemsFlow.value.filterNot { it.userId == userId && it.date == date }
    }

    override suspend fun claimGuestMealPlans(userId: String) {
        itemsFlow.value = itemsFlow.value.map {
            if (it.userId == null) it.copy(userId = userId) else it
        }
    }
}

class FakeGroceryDao : GroceryDao {
    val itemsFlow = MutableStateFlow<List<GroceryItemEntity>>(emptyList())

    override fun getAllGroceryItems(userId: String?): Flow<List<GroceryItemEntity>> {
        return itemsFlow.map { list ->
            list.filter { it.userId == userId }
        }
    }

    override suspend fun getAllGroceryItemsList(userId: String?): List<GroceryItemEntity> {
        return itemsFlow.value.filter { it.userId == userId }
    }

    override suspend fun insertGroceryItem(item: GroceryItemEntity): Long {
        val newId = (itemsFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val entity = item.copy(id = if (item.id == 0L) newId else item.id)
        itemsFlow.value = itemsFlow.value.filter { it.id != entity.id } + entity
        return entity.id
    }

    override suspend fun insertGroceryItems(items: List<GroceryItemEntity>) {
        val current = itemsFlow.value.toMutableList()
        var nextId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        for (item in items) {
            val entity = if (item.id == 0L) item.copy(id = nextId++) else item
            current.removeAll { it.id == entity.id }
            current.add(entity)
        }
        itemsFlow.value = current
    }

    override suspend fun updateGroceryItem(item: GroceryItemEntity) {
        itemsFlow.value = itemsFlow.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun deleteGroceryItemById(id: Long) {
        itemsFlow.value = itemsFlow.value.filter { it.id != id }
    }

    override suspend fun clearCheckedItems(userId: String?) {
        itemsFlow.value = itemsFlow.value.filterNot { it.userId == userId && it.isChecked }
    }

    override suspend fun clearAllItems(userId: String?) {
        itemsFlow.value = itemsFlow.value.filterNot { it.userId == userId }
    }

    override suspend fun claimGuestGroceryItems(userId: String) {
        itemsFlow.value = itemsFlow.value.map {
            if (it.userId == null) it.copy(userId = userId) else it
        }
    }
}

class MealPlanAndGroceryRepositoryTest {

    private lateinit var mealPlanDao: FakeMealPlanDao
    private lateinit var groceryDao: FakeGroceryDao
    private lateinit var authRepository: TestAuthRepository
    private lateinit var mealPlanRepository: MealPlanRepositoryImpl
    private lateinit var groceryRepository: GroceryRepositoryImpl

    @Before
    fun setup() {
        mealPlanDao = FakeMealPlanDao()
        groceryDao = FakeGroceryDao()
        authRepository = TestAuthRepository()
        mealPlanRepository = MealPlanRepositoryImpl(mealPlanDao, authRepository)
        groceryRepository = GroceryRepositoryImpl(groceryDao, authRepository)
    }

    @Test
    fun `add meal plan item stores and retrieves for user`() = runTest {
        authRepository.setUser(AuthUser("user_a", "a@test.com", "Chef A"))

        val item = MealPlanItem(
            date = "2026-10-26",
            slot = MealSlot.LUNCH,
            recipeId = 1,
            recipeTitle = "Spaghetti Bolognese",
            ingredients = listOf("Pasta", "Tomato sauce", "Ground beef")
        )
        mealPlanRepository.addMealPlanItem(item)

        val weekPlans = mealPlanRepository.getMealPlanForWeek("2026-10-26", "2026-11-01").first()
        assertEquals(1, weekPlans.size)
        assertEquals("Spaghetti Bolognese", weekPlans[0].recipeTitle)
        assertEquals("user_a", weekPlans[0].userId)
    }

    @Test
    fun `generateFromMealPlan aggregates and categorizes ingredients correctly`() = runTest {
        authRepository.setUser(AuthUser("user_a", "a@test.com", "Chef A"))

        val mealPlans = listOf(
            MealPlanItem(
                date = "2026-10-26",
                slot = MealSlot.DINNER,
                recipeTitle = "Garlic Butter Steak",
                ingredients = listOf("Beef steak", "Garlic cloves", "Butter", "Black pepper")
            ),
            MealPlanItem(
                date = "2026-10-27",
                slot = MealSlot.BREAKFAST,
                recipeTitle = "Omelette",
                ingredients = listOf("Eggs", "Butter", "Cheese", "Salt") // Note: Butter is duplicate
            )
        )

        val addedCount = groceryRepository.generateFromMealPlan(mealPlans)
        assertEquals(7, addedCount) // 8 ingredients minus 1 duplicate "butter"

        val groceryItems = groceryRepository.getAllGroceryItems().first()
        assertEquals(7, groceryItems.size)

        // Verify categories
        val meatItems = groceryItems.filter { it.category == GroceryCategory.MEAT_SEAFOOD }
        assertTrue(meatItems.any { it.name.contains("steak", ignoreCase = true) })

        val dairyItems = groceryItems.filter { it.category == GroceryCategory.DAIRY_EGGS }
        assertTrue(dairyItems.any { it.name.contains("Eggs", ignoreCase = true) })
        assertTrue(dairyItems.any { it.name.contains("Cheese", ignoreCase = true) })
    }

    @Test
    fun `toggle grocery item checked and clear checked items`() = runTest {
        val itemId = groceryRepository.addGroceryItem(
            com.aayush.recipick.domain.model.GroceryItem(
                name = "Milk",
                category = GroceryCategory.DAIRY_EGGS
            )
        )

        var items = groceryRepository.getAllGroceryItems().first()
        assertEquals(1, items.size)
        assertEquals(false, items[0].isChecked)

        groceryRepository.updateGroceryItem(items[0].copy(isChecked = true))
        items = groceryRepository.getAllGroceryItems().first()
        assertEquals(true, items[0].isChecked)

        groceryRepository.clearCheckedItems()
        items = groceryRepository.getAllGroceryItems().first()
        assertEquals(0, items.size)
    }
}
