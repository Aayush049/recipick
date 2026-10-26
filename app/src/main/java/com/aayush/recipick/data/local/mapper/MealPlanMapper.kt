package com.aayush.recipick.data.local.mapper

import com.aayush.recipick.data.local.entity.MealPlanEntity
import com.aayush.recipick.domain.model.MealPlanItem

fun MealPlanEntity.toDomain(): MealPlanItem {
    return MealPlanItem(
        id = id,
        userId = userId,
        date = date,
        slot = slot,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        isUserRecipe = isUserRecipe,
        customNote = customNote,
        ingredients = ingredients
    )
}

fun MealPlanItem.toEntity(): MealPlanEntity {
    return MealPlanEntity(
        id = id,
        userId = userId,
        date = date,
        slot = slot,
        recipeId = recipeId,
        recipeTitle = recipeTitle,
        recipeImageUrl = recipeImageUrl,
        isUserRecipe = isUserRecipe,
        customNote = customNote,
        ingredients = ingredients
    )
}
