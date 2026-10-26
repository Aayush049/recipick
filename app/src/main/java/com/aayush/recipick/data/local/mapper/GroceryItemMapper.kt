package com.aayush.recipick.data.local.mapper

import com.aayush.recipick.data.local.entity.GroceryItemEntity
import com.aayush.recipick.domain.model.GroceryItem

fun GroceryItemEntity.toDomain(): GroceryItem {
    return GroceryItem(
        id = id,
        userId = userId,
        name = name,
        category = category,
        isChecked = isChecked,
        sourceRecipeTitle = sourceRecipeTitle,
        createdAt = createdAt
    )
}

fun GroceryItem.toEntity(): GroceryItemEntity {
    return GroceryItemEntity(
        id = id,
        userId = userId,
        name = name,
        category = category,
        isChecked = isChecked,
        sourceRecipeTitle = sourceRecipeTitle,
        createdAt = createdAt
    )
}
