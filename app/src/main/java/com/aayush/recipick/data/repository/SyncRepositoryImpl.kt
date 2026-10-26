package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.UserRecipeEntity
import com.aayush.recipick.domain.model.SyncStatus
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class SyncRepositoryImpl(
    private val authRepository: AuthRepository,
    private val userRecipeDao: UserRecipeDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SyncRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun syncRecipes(): Result<Unit> {
        val user = authRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("Must be signed in to sync recipes."))

        _syncStatus.value = SyncStatus.Syncing

        return runCatching {
            // 0. Claim any unassigned guest recipes for the current user
            userRecipeDao.claimGuestRecipes(user.uid)

            val userRecipesRef = firestore
                .collection("users")
                .document(user.uid)
                .collection("recipes")

            // 1. Fetch cloud recipes
            val cloudSnapshot = userRecipesRef.get().await()
            val cloudRecipesMap = cloudSnapshot.documents.associateBy { it.id }

            // 2. Fetch local recipes for current user only
            val localRecipes = userRecipeDao.getRecipesListByUser(user.uid)
            val localRecipesMap = localRecipes.associateBy { it.id.toString() }

            // 3. Upload local recipes that aren't in cloud or are newer
            for (local in localRecipes) {
                val cloudDoc = cloudRecipesMap[local.id.toString()]
                val cloudCreatedAt = cloudDoc?.getLong("createdAt") ?: 0L

                if (cloudDoc == null || local.createdAt >= cloudCreatedAt) {
                    val recipeData = hashMapOf(
                        "id" to local.id,
                        "userId" to user.uid,
                        "name" to local.name,
                        "ingredients" to local.ingredients,
                        "instructions" to local.instructions,
                        "prepTimeMinutes" to local.prepTimeMinutes,
                        "cookTimeMinutes" to local.cookTimeMinutes,
                        "servings" to local.servings,
                        "difficulty" to local.difficulty,
                        "cuisine" to local.cuisine,
                        "caloriesPerServing" to local.caloriesPerServing,
                        "imageUri" to local.imageUri,
                        "createdAt" to local.createdAt
                    )
                    userRecipesRef.document(local.id.toString()).set(recipeData, SetOptions.merge()).await()
                }
            }

            // 4. Download cloud recipes that aren't in local DB
            val toInsertLocally = mutableListOf<UserRecipeEntity>()
            for ((docId, doc) in cloudRecipesMap) {
                if (!localRecipesMap.containsKey(docId)) {
                    val entity = UserRecipeEntity(
                        id = doc.getLong("id")?.toInt() ?: docId.toIntOrNull() ?: 0,
                        userId = user.uid,
                        name = doc.getString("name").orEmpty(),
                        ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        instructions = (doc.get("instructions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        prepTimeMinutes = doc.getLong("prepTimeMinutes")?.toInt() ?: 0,
                        cookTimeMinutes = doc.getLong("cookTimeMinutes")?.toInt() ?: 0,
                        servings = doc.getLong("servings")?.toInt() ?: 1,
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        cuisine = doc.getString("cuisine") ?: "Global",
                        caloriesPerServing = doc.getLong("caloriesPerServing")?.toInt() ?: 0,
                        imageUri = doc.getString("imageUri"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    toInsertLocally.add(entity)
                }
            }

            if (toInsertLocally.isNotEmpty()) {
                userRecipeDao.insertRecipes(toInsertLocally)
            }

            _syncStatus.value = SyncStatus.Success(System.currentTimeMillis())
        }.onFailure { e ->
            _syncStatus.value = SyncStatus.Error(e.localizedMessage ?: "Sync failed")
        }
    }
}
