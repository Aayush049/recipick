package com.aayush.recipick.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aayush.recipick.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans WHERE ((:userId IS NULL AND userId IS NULL) OR (userId = :userId)) AND date >= :startDate AND date <= :endDate ORDER BY date ASC, slot ASC")
    fun getMealPlanForWeek(userId: String?, startDate: String, endDate: String): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(item: MealPlanEntity): Long

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteMealPlanById(id: Long)

    @Query("DELETE FROM meal_plans WHERE ((:userId IS NULL AND userId IS NULL) OR (userId = :userId)) AND date = :date")
    suspend fun clearMealPlanForDate(userId: String?, date: String)

    @Query("UPDATE meal_plans SET userId = :userId WHERE userId IS NULL")
    suspend fun claimGuestMealPlans(userId: String)
}
