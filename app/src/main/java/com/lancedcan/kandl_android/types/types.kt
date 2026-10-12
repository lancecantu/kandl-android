package com.lancedcan.kandl_android

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

enum class RecipeStatus {
    DEFAULT,
    FAVORITED,
    ARCHIVED
}

data class Recipe(
    val id: String = "",
    val name: String = "",
    val recipeText: String = "",
    val status: RecipeStatus = RecipeStatus.DEFAULT,
    val usedCount: Int? = 0,
    val recipeUrl: String? = null,
    val userId: String = "",
    val createdAt: Timestamp? = null
)

data class RecipeWrite(
    val name: String = "",
    val recipeText: String? = null,
    val status: RecipeStatus = RecipeStatus.DEFAULT,
    val usedCount: Int? = 0,
    val recipeUrl: String? = null,
    val userId: String = "",
    val createdAt: Any? = FieldValue.serverTimestamp()
)

/**
 * A plan's own copy of one recipe. Held alongside the id so edits made while building or
 * editing the plan survive without touching the saved recipe.
 */
data class PlanMeal(
    val recipeId: String = "",
    val name: String = "",
    val recipeText: String = ""
)

data class MealPlan(
    val id: String? = "",
    val name: String = "",
    val recipeIdsList: List<String> = emptyList(),
    val meals: List<PlanMeal> = emptyList(),
    val ingredientText: String = "",
    val userId: String = "",
    val createdAt: Timestamp? = null
)

data class MealPlanWrite(
    val name: String? = "",
    val recipeIdsList: List<String>? = emptyList(),
    val meals: List<PlanMeal>? = emptyList(),
    val ingredientText: String? = "",
    val userId: String = "",
    val createdAt: Any? = FieldValue.serverTimestamp()
)
