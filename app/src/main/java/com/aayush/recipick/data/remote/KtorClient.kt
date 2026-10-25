//Actual network call is done here (send request or receive response)
//Communicate with internet using HTTP requests.

package com.aayush.recipick.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorClient {
    val client = HttpClient(Android) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    const val BASE_URL = "https://dummyjson.com/"
//    const val RECIPE = "$BASE_URL/recipes" (we will make End-Point in API Service)
}
