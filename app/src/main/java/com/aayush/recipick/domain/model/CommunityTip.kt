package com.aayush.recipick.domain.model

data class CommunityTip(
    val id: String = "",
    val recipeId: String = "",
    val authorId: String = "",
    val authorName: String = "Helpful Cook",
    val tipText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
