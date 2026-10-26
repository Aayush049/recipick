package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.MealPlanDao
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.MealPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class MealPlanRepositoryImpl(
    private val mealPlanDao: MealPlanDao,
    private val authRepository: AuthRepository? = null
) : MealPlanRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMealPlanForWeek(startDate: String, endDate: String): Flow<List<MealPlanItem>> {
        return if (authRepository != null) {
            authRepository.currentUser.flatMapLatest { user ->
                mealPlanDao.getMealPlanForWeek(user?.uid, startDate, endDate).map { list ->
                    list.map { it.toDomain() }
                }
            }
        } else {
            mealPlanDao.getMealPlanForWeek(null, startDate, endDate).map { list ->
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun addMealPlanItem(item: MealPlanItem): Long {
        val currentUid = authRepository?.currentUser?.value?.uid
        val itemToInsert = if (item.userId == null && currentUid != null) {
            item.copy(userId = currentUid)
        } else {
            item
        }
        return mealPlanDao.insertMealPlan(itemToInsert.toEntity())
    }

    override suspend fun removeMealPlanItem(id: Long) {
        mealPlanDao.deleteMealPlanById(id)
    }

    override suspend fun clearMealPlanForDate(date: String) {
        val currentUid = authRepository?.currentUser?.value?.uid
        mealPlanDao.clearMealPlanForDate(currentUid, date)
    }
}
