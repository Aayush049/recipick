package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.UserRecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRecipeRepositoryImpl(
    private val userRecipeDao: UserRecipeDao
) : UserRecipeRepository {

    override fun getAllUserRecipes(): Flow<List<UserRecipe>> {
        return userRecipeDao.getAllRecipes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUserRecipeById(id: Int): Flow<UserRecipe?> {
        return userRecipeDao.getRecipeById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun insertUserRecipe(recipe: UserRecipe): Long {
        return userRecipeDao.insertRecipe(recipe.toEntity())
    }

    override suspend fun updateUserRecipe(recipe: UserRecipe) {
        userRecipeDao.updateRecipe(recipe.toEntity())
    }

    override suspend fun deleteUserRecipe(recipe: UserRecipe) {
        userRecipeDao.deleteRecipe(recipe.toEntity())
    }

    override suspend fun deleteUserRecipeById(id: Int) {
        userRecipeDao.deleteRecipeById(id)
    }
}
