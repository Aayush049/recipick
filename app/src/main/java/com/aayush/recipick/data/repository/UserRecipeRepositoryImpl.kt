package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRecipeRepositoryImpl(
    private val userRecipeDao: UserRecipeDao,
    private val authRepository: AuthRepository? = null,
    private val firestore: FirebaseFirestore? = null
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
        deleteFromCloud(recipe.id, recipe.userId)
    }

    override suspend fun deleteUserRecipeById(id: Int) {
        userRecipeDao.deleteRecipeById(id)
        deleteFromCloud(id, null)
    }

    private suspend fun deleteFromCloud(recipeId: Int, recipeUserId: String?) {
        val currentUid = authRepository?.currentUser?.value?.uid ?: recipeUserId
        val db = firestore
        if (currentUid != null && db != null) {
            runCatching {
                db.collection("users")
                    .document(currentUid)
                    .collection("recipes")
                    .document(recipeId.toString())
                    .delete()
                    .await()
            }
        }
    }
}
