package com.lancedcan.kandl_android

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecipeManagerViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _editRecipeId = MutableStateFlow<String?>(null)
    private val _editTitle = MutableStateFlow("")
    val editTitle: StateFlow<String> = _editTitle.asStateFlow()

    private val _editRecipeText = MutableStateFlow("")
    val editRecipeText: StateFlow<String> = _editRecipeText.asStateFlow()

    private val _editRecipeStatus = MutableStateFlow(RecipeStatus.DEFAULT)
    val editRecipeStatus: StateFlow<RecipeStatus> = _editRecipeStatus.asStateFlow()

    fun setEditTitle(value: String) {
        _editTitle.value = value
    }

    fun setEditRecipeText(value: String) {
        _editRecipeText.value = value
    }

    fun startNewRecipe() {
        _editRecipeId.value = null
        _editTitle.value = ""
        _editRecipeText.value = ""
        _editRecipeStatus.value = RecipeStatus.DEFAULT
        _originalEditTitle.value = ""
        _originalEditRecipeText.value = ""
    }

    private val _originalEditTitle = MutableStateFlow("")
    private val _originalEditRecipeText = MutableStateFlow("")

    val originalEditTitle: StateFlow<String> = _originalEditTitle.asStateFlow()
    val originalEditRecipeText: StateFlow<String> = _originalEditRecipeText.asStateFlow()

    fun loadRecipeForEdit(recipeId: String) {
        val recipe = _recipes.value.find { it.id == recipeId }
        if (recipe != null) {
            _editRecipeId.value = recipe.id
            _editTitle.value = recipe.name
            _editRecipeText.value = recipe.recipeText
            _editRecipeStatus.value = recipe.status
            _originalEditTitle.value = recipe.name
            _originalEditRecipeText.value = recipe.recipeText
        }
    }

    /**
     * Primes the edit fields from a meal plan's copy of a recipe, which may already differ
     * from what is stored under [recipeId].
     */
    fun startEditingMealPlanCopy(recipeId: String?, name: String, recipeText: String) {
        _editRecipeId.value = recipeId
        _editTitle.value = name
        _editRecipeText.value = recipeText
        _editRecipeStatus.value = _recipes.value.find { it.id == recipeId }?.status
            ?: RecipeStatus.DEFAULT
        _originalEditTitle.value = name
        _originalEditRecipeText.value = recipeText
    }

    /**
     * Writes the edited title and body back onto the source recipe, leaving its other
     * fields (status, usage count, timestamps) as they are.
     */
    fun updateOriginalRecipe() {
        val id = _editRecipeId.value
        if (id.isNullOrBlank()) return
        val name = _editTitle.value.trim()
        val recipeText = _editRecipeText.value.trim()
        if (name.isBlank() || recipeText.isBlank()) return
        firestore.collection("recipes").document(id)
            .update(mapOf("name" to name, "recipeText" to recipeText))
            .addOnSuccessListener { loadRecipes() }
    }

    /**
     * Deletes the recipe from the DB. No-op if id is null/blank.
     */
    fun deleteRecipe(id: String?) {
        if (id.isNullOrBlank()) return
        firestore.collection("recipes").document(id)
            .delete()
            .addOnSuccessListener { loadRecipes() }
    }

    /**
     * Updates only the recipe status in the DB (title/body are not saved).
     * No-op if not editing an existing recipe.
     */
    fun updateRecipeStatus(newStatus: RecipeStatus) {
        val id = _editRecipeId.value ?: return
        firestore.collection("recipes").document(id)
            .update("status", newStatus.name)
            .addOnSuccessListener {
                _editRecipeStatus.value = newStatus
                loadRecipes()
            }
    }

    fun saveRecipe() {
        val user = firebaseAuth.currentUser ?: return
        val name = _editTitle.value.trim()
        val recipeText = _editRecipeText.value.trim()
        if (name.isBlank() || recipeText.isBlank()) return

        val write = RecipeWrite(
            name = name,
            recipeText = recipeText,
            status = _editRecipeStatus.value,
            usedCount = 0,
            recipeUrl = null,
            userId = user.uid
        )

        val id = _editRecipeId.value
        if (id.isNullOrBlank()) {
            firestore.collection("recipes")
                .add(write)
                .addOnSuccessListener { loadRecipes() }
        } else {
            firestore.collection("recipes").document(id)
                .set(write)
                .addOnSuccessListener { loadRecipes() }
        }
    }

    fun loadRecipes() {
        val user = firebaseAuth.currentUser ?: return
        _isLoading.value = true
        firestore.collection("recipes")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                _recipes.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.copy(id = doc.id)
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
}
