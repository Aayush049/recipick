//DTO: Data Transfer Object (It provides Structure to the received data from the JSON Server.)
//It helps in mapping the response to the data class
package com.aayush.recipick.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    val limit: Int,
    val recipes: List<Recipe>,
    val skip: Int,
    val total: Int
)