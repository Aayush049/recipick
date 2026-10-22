//All function related to API service written here

package com.aayush.recipick.data.remote

import com.aayush.recipick.data.remote.dto.AddRecipeRequest
import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.data.remote.dto.RecipeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RecipeAPIService(private val client: HttpClient) {

    suspend fun getAllRecipes() : RecipeResponse{
        return client.get(urlString = "${KtorClient.BASE_URL}recipes").body()
    }

    suspend fun getRecipeByID(id: Int) : Recipe{
        return client.get(urlString = "${KtorClient.BASE_URL}recipes/$id").body()
    }

    suspend fun addRecipe(addRequest: AddRecipeRequest){
        client.post(urlString = "${KtorClient.BASE_URL}recipes/add"){
            contentType(ContentType.Application.Json)
            setBody(addRequest)
        }
    }

}