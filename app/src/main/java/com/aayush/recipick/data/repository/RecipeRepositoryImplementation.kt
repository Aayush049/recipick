package com.aayush.recipick.data.repository

import com.aayush.recipick.data.remote.RecipeAPIService
import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.domain.repository.RecipeRepository

class RecipeRepositoryImplementation(private val apiService: RecipeAPIService) : RecipeRepository  {

    override suspend fun getAllRecipes(): List<Recipe> {
        return apiService.getAllRecipes().recipes
    }

    override suspend fun getRecipeByID(id: Int): Recipe {
        return apiService.getRecipeByID(id)
    }
}