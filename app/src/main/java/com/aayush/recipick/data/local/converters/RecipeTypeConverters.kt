package com.aayush.recipick.data.local.converters

import androidx.room.TypeConverter
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.MealSlot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeTypeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else json.decodeFromString(value)
    }

    @TypeConverter
    fun fromMealSlot(slot: MealSlot): String {
        return slot.name
    }

    @TypeConverter
    fun toMealSlot(value: String): MealSlot {
        return runCatching { MealSlot.valueOf(value) }.getOrDefault(MealSlot.BREAKFAST)
    }

    @TypeConverter
    fun fromGroceryCategory(category: GroceryCategory): String {
        return category.name
    }

    @TypeConverter
    fun toGroceryCategory(value: String): GroceryCategory {
        return runCatching { GroceryCategory.valueOf(value) }.getOrDefault(GroceryCategory.OTHER)
    }
}
