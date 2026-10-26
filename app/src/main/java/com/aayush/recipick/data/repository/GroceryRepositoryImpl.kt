package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.GroceryDao
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.GroceryItem
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.GroceryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GroceryRepositoryImpl(
    private val groceryDao: GroceryDao,
    private val authRepository: AuthRepository? = null
) : GroceryRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllGroceryItems(): Flow<List<GroceryItem>> {
        return if (authRepository != null) {
            authRepository.currentUser.flatMapLatest { user ->
                groceryDao.getAllGroceryItems(user?.uid).map { list ->
                    list.map { it.toDomain() }
                }
            }
        } else {
            groceryDao.getAllGroceryItems(null).map { list ->
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun addGroceryItem(item: GroceryItem): Long {
        val currentUid = authRepository?.currentUser?.value?.uid
        val itemToInsert = if (item.userId == null && currentUid != null) {
            item.copy(userId = currentUid)
        } else {
            item
        }
        return groceryDao.insertGroceryItem(itemToInsert.toEntity())
    }

    override suspend fun addGroceryItems(items: List<GroceryItem>) {
        val currentUid = authRepository?.currentUser?.value?.uid
        val itemsToInsert = items.map { item ->
            if (item.userId == null && currentUid != null) {
                item.copy(userId = currentUid)
            } else {
                item
            }
        }
        groceryDao.insertGroceryItems(itemsToInsert.map { it.toEntity() })
    }

    override suspend fun updateGroceryItem(item: GroceryItem) {
        groceryDao.updateGroceryItem(item.toEntity())
    }

    override suspend fun deleteGroceryItem(id: Long) {
        groceryDao.deleteGroceryItemById(id)
    }

    override suspend fun clearCheckedItems() {
        val currentUid = authRepository?.currentUser?.value?.uid
        groceryDao.clearCheckedItems(currentUid)
    }

    override suspend fun clearAllItems() {
        val currentUid = authRepository?.currentUser?.value?.uid
        groceryDao.clearAllItems(currentUid)
    }

    override suspend fun generateFromMealPlan(mealPlans: List<MealPlanItem>): Int {
        val currentUid = authRepository?.currentUser?.value?.uid
        val seenNames = groceryDao.getAllGroceryItemsList(currentUid).map { it.name.lowercase().trim() }.toMutableSet()

        val itemsToInsert = mutableListOf<GroceryItem>()

        for (plan in mealPlans) {
            for (rawIngredient in plan.ingredients) {
                val trimmed = rawIngredient.trim()
                val lower = trimmed.lowercase()
                if (trimmed.isNotBlank() && !seenNames.contains(lower)) {
                    seenNames.add(lower)
                    val category = IngredientCategorizer.categorize(trimmed)
                    itemsToInsert.add(
                        GroceryItem(
                            userId = currentUid,
                            name = trimmed,
                            category = category,
                            isChecked = false,
                            sourceRecipeTitle = plan.recipeTitle
                        )
                    )
                }
            }
        }

        if (itemsToInsert.isNotEmpty()) {
            addGroceryItems(itemsToInsert)
        }

        return itemsToInsert.size
    }
}
