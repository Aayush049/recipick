package com.aayush.recipick

import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.data.local.entity.UserRecipeEntity
import com.aayush.recipick.data.local.mapper.toDomain
import com.aayush.recipick.data.local.mapper.toEntity
import com.aayush.recipick.domain.model.UserRecipe
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeConvertersAndMapperTest {

    private val converters = RecipeTypeConverters()

    @Test
    fun testStringListSerialization() {
        val list = listOf("Flour", "Sugar", "Eggs")
        val jsonString = converters.fromStringList(list)
        val decodedList = converters.toStringList(jsonString)

        assertEquals(list, decodedList)
    }

    @Test
    fun testEmptyStringListSerialization() {
        val list = emptyList<String>()
        val jsonString = converters.fromStringList(list)
        val decodedList = converters.toStringList(jsonString)

        assertEquals(list, decodedList)
    }

    @Test
    fun testEntityDomainMapping() {
        val domain = UserRecipe(
            id = 42,
            userId = "test_user_123",
            name = "Pancakes",
            ingredients = listOf("Flour", "Milk"),
            instructions = listOf("Mix", "Cook"),
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = "Easy",
            cuisine = "American",
            caloriesPerServing = 250,
            imageUri = "content://media/123",
            createdAt = 1000L
        )

        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.userId, entity.userId)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.ingredients, entity.ingredients)
        assertEquals(domain.instructions, entity.instructions)
        assertEquals(domain.prepTimeMinutes, entity.prepTimeMinutes)
        assertEquals(domain.cookTimeMinutes, entity.cookTimeMinutes)
        assertEquals(domain.servings, entity.servings)
        assertEquals(domain.difficulty, entity.difficulty)
        assertEquals(domain.cuisine, entity.cuisine)
        assertEquals(domain.caloriesPerServing, entity.caloriesPerServing)
        assertEquals(domain.imageUri, entity.imageUri)
        assertEquals(domain.createdAt, entity.createdAt)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }
}
