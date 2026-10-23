package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.UserRecipe
import kotlinx.coroutines.flow.Flow

interface UserRecipeRepository {
    fun getAllUserRecipes(): Flow<List<UserRecipe>>
    fun getUserRecipeById(id: Int): Flow<UserRecipe?>
    suspend fun insertUserRecipe(recipe: UserRecipe): Long
    suspend fun updateUserRecipe(recipe: UserRecipe)
    suspend fun deleteUserRecipe(recipe: UserRecipe)
    suspend fun deleteUserRecipeById(id: Int)
}
