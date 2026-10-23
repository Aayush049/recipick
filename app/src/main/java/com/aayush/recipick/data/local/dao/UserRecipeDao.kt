package com.aayush.recipick.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aayush.recipick.data.local.entity.UserRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRecipeDao {
    @Query("SELECT * FROM user_recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<UserRecipeEntity>>

    @Query("SELECT * FROM user_recipes WHERE id = :id")
    fun getRecipeById(id: Int): Flow<UserRecipeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: UserRecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: UserRecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: UserRecipeEntity)

    @Query("DELETE FROM user_recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Int)
}
