package com.aayush.recipick.data.repository

import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.UserRecipeEntity
import com.aayush.recipick.domain.model.CommunityRecipe
import com.aayush.recipick.domain.model.CommunityTip
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.AuthRepository
import com.aayush.recipick.domain.repository.CommunityRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlin.math.round

class CommunityRepositoryImpl(
    private val authRepository: AuthRepository,
    private val userRecipeDao: UserRecipeDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CommunityRepository {

    override fun getCommunityRecipes(): Flow<List<CommunityRecipe>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_RECIPES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CommunityRecipe(
                            id = doc.id,
                            authorId = doc.getString("authorId").orEmpty(),
                            authorName = doc.getString("authorName") ?: "Community Chef",
                            name = doc.getString("name").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            instructions = (doc.get("instructions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            prepTimeMinutes = doc.getLong("prepTimeMinutes")?.toInt() ?: 0,
                            cookTimeMinutes = doc.getLong("cookTimeMinutes")?.toInt() ?: 0,
                            servings = doc.getLong("servings")?.toInt() ?: 1,
                            difficulty = doc.getString("difficulty") ?: "Medium",
                            cuisine = doc.getString("cuisine") ?: "General",
                            caloriesPerServing = doc.getLong("caloriesPerServing")?.toInt() ?: 0,
                            imageUrl = doc.getString("imageUrl"),
                            averageRating = doc.getDouble("averageRating") ?: 0.0,
                            ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0,
                            tipCount = doc.getLong("tipCount")?.toInt() ?: 0,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(recipes)
            }
        awaitClose { listener.remove() }
    }

    override fun getCommunityRecipeById(recipeId: String): Flow<CommunityRecipe?> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_RECIPES)
            .document(recipeId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                try {
                    val recipe = CommunityRecipe(
                        id = doc.id,
                        authorId = doc.getString("authorId").orEmpty(),
                        authorName = doc.getString("authorName") ?: "Community Chef",
                        name = doc.getString("name").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        ingredients = (doc.get("ingredients") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        instructions = (doc.get("instructions") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        prepTimeMinutes = doc.getLong("prepTimeMinutes")?.toInt() ?: 0,
                        cookTimeMinutes = doc.getLong("cookTimeMinutes")?.toInt() ?: 0,
                        servings = doc.getLong("servings")?.toInt() ?: 1,
                        difficulty = doc.getString("difficulty") ?: "Medium",
                        cuisine = doc.getString("cuisine") ?: "General",
                        caloriesPerServing = doc.getLong("caloriesPerServing")?.toInt() ?: 0,
                        imageUrl = doc.getString("imageUrl"),
                        averageRating = doc.getDouble("averageRating") ?: 0.0,
                        ratingCount = doc.getLong("ratingCount")?.toInt() ?: 0,
                        tipCount = doc.getLong("tipCount")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                    trySend(recipe)
                } catch (_: Exception) {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun shareRecipeToCommunity(userRecipe: UserRecipe, authorName: String): Result<String> {
        val user = authRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("You must be signed in to share recipes to the community."))

        return runCatching {
            val docRef = firestore.collection(COLLECTION_COMMUNITY_RECIPES).document()
            val effectiveAuthor = authorName.trim().ifEmpty {
                user.displayName?.trim()?.ifEmpty { "Community Chef" } ?: "Community Chef"
            }

            val data = hashMapOf(
                "id" to docRef.id,
                "authorId" to user.uid,
                "authorName" to effectiveAuthor,
                "name" to userRecipe.name,
                "description" to "${userRecipe.cuisine} • ${userRecipe.difficulty} • Ready in ${userRecipe.prepTimeMinutes + userRecipe.cookTimeMinutes} mins",
                "ingredients" to userRecipe.ingredients,
                "instructions" to userRecipe.instructions,
                "prepTimeMinutes" to userRecipe.prepTimeMinutes,
                "cookTimeMinutes" to userRecipe.cookTimeMinutes,
                "servings" to userRecipe.servings,
                "difficulty" to userRecipe.difficulty,
                "cuisine" to userRecipe.cuisine,
                "caloriesPerServing" to userRecipe.caloriesPerServing,
                "imageUrl" to userRecipe.imageUri,
                "averageRating" to 0.0,
                "ratingCount" to 0,
                "tipCount" to 0,
                "createdAt" to System.currentTimeMillis()
            )
            docRef.set(data).await()
            docRef.id
        }
    }

    override suspend fun rateRecipe(recipeId: String, rating: Int): Result<Unit> {
        val user = authRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("You must be signed in to rate a recipe."))

        if (rating !in 1..5) {
            return Result.failure(IllegalArgumentException("Rating must be between 1 and 5."))
        }

        return runCatching {
            val recipeRef = firestore.collection(COLLECTION_COMMUNITY_RECIPES).document(recipeId)
            val ratingRef = recipeRef.collection(SUBCOLLECTION_RATINGS).document(user.uid)

            firestore.runTransaction { transaction ->
                val ratingDoc = transaction.get(ratingRef)
                val recipeDoc = transaction.get(recipeRef)

                val currentAvg = recipeDoc.getDouble("averageRating") ?: 0.0
                val currentCount = recipeDoc.getLong("ratingCount")?.toInt() ?: 0

                val previousRating = if (ratingDoc.exists()) ratingDoc.getLong("rating")?.toInt() else null

                val (newAvg, newCount) = if (previousRating != null) {
                    val total = (currentAvg * currentCount) - previousRating + rating
                    val calculated = if (currentCount > 0) total / currentCount else rating.toDouble()
                    (round(calculated * 10.0) / 10.0) to currentCount
                } else {
                    val count = currentCount + 1
                    val calculated = ((currentAvg * currentCount) + rating) / count
                    (round(calculated * 10.0) / 10.0) to count
                }

                transaction.update(recipeRef, "averageRating", newAvg, "ratingCount", newCount)
                transaction.set(
                    ratingRef,
                    hashMapOf(
                        "userId" to user.uid,
                        "rating" to rating,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            }.await()
        }
    }

    override fun getUserRating(recipeId: String): Flow<Int?> {
        val user = authRepository.currentUser.value ?: return flowOf(null)
        return callbackFlow {
            val listener = firestore.collection(COLLECTION_COMMUNITY_RECIPES)
                .document(recipeId)
                .collection(SUBCOLLECTION_RATINGS)
                .document(user.uid)
                .addSnapshotListener { doc, error ->
                    if (error != null || doc == null || !doc.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(doc.getLong("rating")?.toInt())
                }
            awaitClose { listener.remove() }
        }
    }

    override fun getRecipeTips(recipeId: String): Flow<List<CommunityTip>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_RECIPES)
            .document(recipeId)
            .collection(SUBCOLLECTION_TIPS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tips = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        CommunityTip(
                            id = doc.id,
                            recipeId = recipeId,
                            authorId = doc.getString("authorId").orEmpty(),
                            authorName = doc.getString("authorName") ?: "Helpful Cook",
                            tipText = doc.getString("tipText").orEmpty(),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(tips)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addRecipeTip(recipeId: String, tipText: String, authorName: String): Result<String> {
        val user = authRepository.currentUser.value
            ?: return Result.failure(IllegalStateException("You must be signed in to post cooking tips."))

        val trimmedTip = tipText.trim()
        if (trimmedTip.isBlank()) {
            return Result.failure(IllegalArgumentException("Tip text cannot be empty."))
        }

        return runCatching {
            val recipeRef = firestore.collection(COLLECTION_COMMUNITY_RECIPES).document(recipeId)
            val tipRef = recipeRef.collection(SUBCOLLECTION_TIPS).document()

            val effectiveAuthor = authorName.trim().ifEmpty {
                user.displayName?.trim()?.ifEmpty { "Helpful Cook" } ?: "Helpful Cook"
            }

            val tipData = hashMapOf(
                "id" to tipRef.id,
                "recipeId" to recipeId,
                "authorId" to user.uid,
                "authorName" to effectiveAuthor,
                "tipText" to trimmedTip,
                "createdAt" to System.currentTimeMillis()
            )

            tipRef.set(tipData).await()
            recipeRef.update("tipCount", FieldValue.increment(1)).await()
            tipRef.id
        }
    }

    override suspend fun saveCommunityRecipeToPersonal(communityRecipe: CommunityRecipe): Result<Long> {
        return runCatching {
            val entity = UserRecipeEntity(
                userId = authRepository.currentUser.value?.uid,
                name = communityRecipe.name,
                ingredients = communityRecipe.ingredients,
                instructions = communityRecipe.instructions,
                prepTimeMinutes = communityRecipe.prepTimeMinutes,
                cookTimeMinutes = communityRecipe.cookTimeMinutes,
                servings = communityRecipe.servings,
                difficulty = communityRecipe.difficulty,
                cuisine = communityRecipe.cuisine,
                caloriesPerServing = communityRecipe.caloriesPerServing,
                imageUri = communityRecipe.imageUrl,
                createdAt = System.currentTimeMillis()
            )
            userRecipeDao.insertRecipe(entity)
        }
    }

    companion object {
        private const val COLLECTION_COMMUNITY_RECIPES = "community_recipes"
        private const val SUBCOLLECTION_RATINGS = "ratings"
        private const val SUBCOLLECTION_TIPS = "tips"
    }
}
