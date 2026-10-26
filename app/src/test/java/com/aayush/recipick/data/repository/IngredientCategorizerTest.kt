package com.aayush.recipick.data.repository

import com.aayush.recipick.domain.model.GroceryCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientCategorizerTest {

    @Test
    fun `categorize produce ingredients`() {
        assertEquals(GroceryCategory.PRODUCE, IngredientCategorizer.categorize("2 medium chopped onions"))
        assertEquals(GroceryCategory.PRODUCE, IngredientCategorizer.categorize("3 cloves minced garlic"))
        assertEquals(GroceryCategory.PRODUCE, IngredientCategorizer.categorize("1 cup cherry tomatoes"))
        assertEquals(GroceryCategory.PRODUCE, IngredientCategorizer.categorize("Fresh basil leaves"))
        assertEquals(GroceryCategory.PRODUCE, IngredientCategorizer.categorize("1 ripe avocado"))
    }

    @Test
    fun `categorize dairy and eggs ingredients`() {
        assertEquals(GroceryCategory.DAIRY_EGGS, IngredientCategorizer.categorize("2 large eggs"))
        assertEquals(GroceryCategory.DAIRY_EGGS, IngredientCategorizer.categorize("1 cup whole milk"))
        assertEquals(GroceryCategory.DAIRY_EGGS, IngredientCategorizer.categorize("Grated parmesan cheese"))
        assertEquals(GroceryCategory.DAIRY_EGGS, IngredientCategorizer.categorize("2 tbsp unsalted butter"))
        assertEquals(GroceryCategory.DAIRY_EGGS, IngredientCategorizer.categorize("Greek yogurt"))
    }

    @Test
    fun `categorize meat and seafood ingredients`() {
        assertEquals(GroceryCategory.MEAT_SEAFOOD, IngredientCategorizer.categorize("500g skinless chicken breasts"))
        assertEquals(GroceryCategory.MEAT_SEAFOOD, IngredientCategorizer.categorize("1 lb ground beef"))
        assertEquals(GroceryCategory.MEAT_SEAFOOD, IngredientCategorizer.categorize("Fresh salmon fillets"))
        assertEquals(GroceryCategory.MEAT_SEAFOOD, IngredientCategorizer.categorize("Peeled and deveined shrimp"))
        assertEquals(GroceryCategory.MEAT_SEAFOOD, IngredientCategorizer.categorize("Crispy bacon strips"))
    }

    @Test
    fun `categorize bakery and grains ingredients`() {
        assertEquals(GroceryCategory.BAKERY_GRAINS, IngredientCategorizer.categorize("2 slices whole wheat bread"))
        assertEquals(GroceryCategory.BAKERY_GRAINS, IngredientCategorizer.categorize("2 cups all-purpose flour"))
        assertEquals(GroceryCategory.BAKERY_GRAINS, IngredientCategorizer.categorize("Dried spaghetti pasta"))
        assertEquals(GroceryCategory.BAKERY_GRAINS, IngredientCategorizer.categorize("1 cup jasmine rice"))
        assertEquals(GroceryCategory.BAKERY_GRAINS, IngredientCategorizer.categorize("Corn tortillas"))
    }

    @Test
    fun `categorize pantry and spices ingredients`() {
        assertEquals(GroceryCategory.PANTRY_SPICES, IngredientCategorizer.categorize("1 tsp sea salt"))
        assertEquals(GroceryCategory.PANTRY_SPICES, IngredientCategorizer.categorize("Extra virgin olive oil"))
        assertEquals(GroceryCategory.PANTRY_SPICES, IngredientCategorizer.categorize("1 tbsp soy sauce"))
        assertEquals(GroceryCategory.PANTRY_SPICES, IngredientCategorizer.categorize("Ground cinnamon"))
        assertEquals(GroceryCategory.PANTRY_SPICES, IngredientCategorizer.categorize("1 can black beans"))
    }

    @Test
    fun `categorize unknown ingredients as other`() {
        assertEquals(GroceryCategory.OTHER, IngredientCategorizer.categorize("Mystery spice blend"))
    }
}
