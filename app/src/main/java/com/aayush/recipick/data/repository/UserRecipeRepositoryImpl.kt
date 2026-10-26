package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class UserRecipeRepositoryImpl(
    private val userRecipeDao: UserRecipeDao,
    private val authRepository: AuthRepository? = null
) : UserRecipeRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllUserRecipes(): Flow<List<UserRecipe>> {
        return if (authRepository != null) {
            authRepository.currentUser.flatMapLatest { user ->
                userRecipeDao.getRecipesByUser(user?.uid).map { entities ->
                    entities.map { it.toDomain() }
                }
            }
        } else {
            userRecipeDao.getAllRecipes().map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    override fun getUserRecipeById(id: Int): Flow<UserRecipe?> {
        return userRecipeDao.getRecipeById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertUserRecipe(recipe: UserRecipe): Long {
        val currentUid = authRepository?.currentUser?.value?.uid
        val recipeToInsert = if (recipe.userId == null && currentUid != null) {
            recipe.copy(userId = currentUid)
        } else {
            recipe
        }
        return userRecipeDao.insertRecipe(recipeToInsert.toEntity())
    }

    override suspend fun updateUserRecipe(recipe: UserRecipe) {
        val currentUid = authRepository?.currentUser?.value?.uid
        val recipeToUpdate = if (recipe.userId == null && currentUid != null) {
            recipe.copy(userId = currentUid)
        } else {
            recipe
        }
        userRecipeDao.updateRecipe(recipeToUpdate.toEntity())
    }

    override suspend fun deleteUserRecipe(recipe: UserRecipe) {
        userRecipeDao.deleteRecipe(recipe.toEntity())
    }

    override suspend fun deleteUserRecipeById(id: Int) {
        userRecipeDao.deleteRecipeById(id)
    }
}
