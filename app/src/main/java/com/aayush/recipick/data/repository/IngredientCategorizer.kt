package com.aayush.recipick.data.repository

import com.aayush.recipick.domain.model.GroceryCategory

object IngredientCategorizer {
    private val produceKeywords = listOf(
        "onion", "garlic", "tomato", "potato", "carrot", "lettuce", "spinach", "kale",
        "lemon", "lime", "apple", "banana", "avocado", "cilantro", "basil", "parsley",
        "pepper", "bell pepper", "jalapeno", "mushroom", "broccoli", "cauliflower",
        "cucumber", "zucchini", "ginger", "scallion", "shallot", "corn", "berry", "fruit",
        "vegetable", "celery", "cabbage", "peas", "bean sprout", "squash", "herb", "herbs"
    )

    private val dairyEggsKeywords = listOf(
        "milk", "cheese", "cheddar", "mozzarella", "parmesan", "butter", "egg", "eggs", "cream",
        "yogurt", "sour cream", "ghee", "heavy cream", "buttermilk", "paneer", "ricotta"
    )

    private val meatSeafoodKeywords = listOf(
        "chicken", "beef", "pork", "steak", "bacon", "salmon", "shrimp", "fish", "turkey",
        "lamb", "tuna", "prawn", "crab", "sausage", "ham", "ground beef", "meat"
    )

    private val bakeryGrainsKeywords = listOf(
        "bread", "flour", "tortilla", "rice", "pasta", "spaghetti", "noodle", "oats",
        "quinoa", "bun", "bagel", "pita", "crouton", "crust", "dough", "cereal"
    )

    private val pantrySpicesKeywords = listOf(
        "salt", "black pepper", "oil", "olive oil", "vegetable oil", "sugar", "brown sugar",
        "cinnamon", "oregano", "cumin", "paprika", "turmeric", "soy sauce", "vinegar",
        "sauce", "mustard", "mayonnaise", "ketchup", "honey", "syrup", "broth", "stock",
        "vanilla", "baking powder", "baking soda", "yeast", "curry powder", "chili powder",
        "beans", "can", "canned", "lentils", "chickpeas", "nuts", "almond", "walnut", "peanut"
    )

    fun categorize(ingredient: String): GroceryCategory {
        val lower = ingredient.lowercase()
        return when {
            dairyEggsKeywords.any { lower.contains(it) } -> GroceryCategory.DAIRY_EGGS
            meatSeafoodKeywords.any { lower.contains(it) } -> GroceryCategory.MEAT_SEAFOOD
            bakeryGrainsKeywords.any { lower.contains(it) } -> GroceryCategory.BAKERY_GRAINS
            produceKeywords.any { lower.contains(it) } -> GroceryCategory.PRODUCE
            pantrySpicesKeywords.any { lower.contains(it) } -> GroceryCategory.PANTRY_SPICES
            else -> GroceryCategory.OTHER
        }
    }
}
