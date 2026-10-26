package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.GroceryItem
import com.aayush.recipick.domain.model.MealPlanItem
import kotlinx.coroutines.flow.Flow

interface GroceryRepository {
    fun getAllGroceryItems(): Flow<List<GroceryItem>>
    suspend fun addGroceryItem(item: GroceryItem): Long
    suspend fun addGroceryItems(items: List<GroceryItem>)
    suspend fun updateGroceryItem(item: GroceryItem)
    suspend fun deleteGroceryItem(id: Long)
    suspend fun clearCheckedItems()
    suspend fun clearAllItems()
    suspend fun generateFromMealPlan(mealPlans: List<MealPlanItem>): Int
}
