package com.aayush.recipick.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aayush.recipick.data.local.converters.RecipeTypeConverters

@Entity(tableName = "user_recipes")
@TypeConverters(RecipeTypeConverters::class)
data class UserRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String? = null,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val cuisine: String,
    val caloriesPerServing: Int = 0,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
