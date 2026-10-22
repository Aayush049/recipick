package com.aayush.recipick.domain.repository

import com.aayush.recipick.data.remote.dto.Recipe

interface RecipeRepository {

    suspend fun getAllRecipes(): List<Recipe>

    suspend fun getRecipeByID(id: Int): Recipe

}