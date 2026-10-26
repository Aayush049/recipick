package com.aayush.recipick.data.local.mapper

import com.aayush.recipick.data.local.entity.UserRecipeEntity
import com.aayush.recipick.domain.model.UserRecipe

fun UserRecipeEntity.toDomain(): UserRecipe {
    return UserRecipe(
        id = id,
        userId = userId,
        name = name,
        ingredients = ingredients,
        instructions = instructions,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty,
        cuisine = cuisine,
        caloriesPerServing = caloriesPerServing,
        imageUri = imageUri,
        createdAt = createdAt
    )
}

fun UserRecipe.toEntity(): UserRecipeEntity {
    return UserRecipeEntity(
        id = id,
        userId = userId,
        name = name,
        ingredients = ingredients,
        instructions = instructions,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty,
        cuisine = cuisine,
        caloriesPerServing = caloriesPerServing,
        imageUri = imageUri,
        createdAt = createdAt
    )
}
