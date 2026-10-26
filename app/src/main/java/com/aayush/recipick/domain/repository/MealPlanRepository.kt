package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.MealPlanItem
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    fun getMealPlanForWeek(startDate: String, endDate: String): Flow<List<MealPlanItem>>
    suspend fun addMealPlanItem(item: MealPlanItem): Long
    suspend fun removeMealPlanItem(id: Long)
    suspend fun clearMealPlanForDate(date: String)
}
