package com.aayush.recipick.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.domain.model.GroceryCategory

@Entity(tableName = "grocery_items")
@TypeConverters(RecipeTypeConverters::class)
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String? = null,
    val name: String,
    val category: GroceryCategory = GroceryCategory.OTHER,
    val isChecked: Boolean = false,
    val sourceRecipeTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
