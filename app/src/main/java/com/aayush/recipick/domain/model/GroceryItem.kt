package com.aayush.recipick.domain.model

data class GroceryItem(
    val id: Long = 0,
    val userId: String? = null,
    val name: String,
    val category: GroceryCategory = GroceryCategory.OTHER,
    val isChecked: Boolean = false,
    val sourceRecipeTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
