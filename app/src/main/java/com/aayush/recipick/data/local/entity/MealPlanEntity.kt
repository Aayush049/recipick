package com.aayush.recipick.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.domain.model.MealSlot

@Entity(tableName = "meal_plans")
@TypeConverters(RecipeTypeConverters::class)
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String? = null,
    val date: String,
    val slot: MealSlot,
    val recipeId: Int? = null,
    val recipeTitle: String,
    val recipeImageUrl: String? = null,
    val isUserRecipe: Boolean = false,
    val customNote: String? = null,
    val ingredients: List<String> = emptyList()
)
