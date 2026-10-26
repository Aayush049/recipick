package com.aayush.recipick.domain.model

enum class GroceryCategory(val displayName: String, val iconEmoji: String) {
    PRODUCE("Produce", "🥦"),
    DAIRY_EGGS("Dairy & Eggs", "🥛"),
    MEAT_SEAFOOD("Meat & Seafood", "🥩"),
    BAKERY_GRAINS("Bakery & Grains", "🌾"),
    PANTRY_SPICES("Pantry & Spices", "🧂"),
    OTHER("Other", "📦")
}
