package com.aayush.recipick.domain.repository

import com.aayush.recipick.domain.model.CommunityRecipe
import com.aayush.recipick.domain.model.CommunityTip
import com.aayush.recipick.domain.model.UserRecipe
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunityRecipes(): Flow<List<CommunityRecipe>>
    fun getCommunityRecipeById(recipeId: String): Flow<CommunityRecipe?>
    suspend fun shareRecipeToCommunity(userRecipe: UserRecipe, authorName: String): Result<String>
    suspend fun rateRecipe(recipeId: String, rating: Int): Result<Unit>
    fun getUserRating(recipeId: String): Flow<Int?>
    fun getRecipeTips(recipeId: String): Flow<List<CommunityTip>>
    suspend fun addRecipeTip(recipeId: String, tipText: String, authorName: String): Result<String>
    suspend fun saveCommunityRecipeToPersonal(communityRecipe: CommunityRecipe): Result<Long>
}
