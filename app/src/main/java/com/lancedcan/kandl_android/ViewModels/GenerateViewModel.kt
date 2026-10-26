package com.lancedcan.kandl_android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.StreamResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * A meal plan's own copy of a recipe. Edits made while building the plan land here and
 * leave the saved recipe untouched. [isEdited] marks a copy whose changes live only in
 * this plan, so re-rolling the slot would throw them away.
 */
data class MealSlot(
    val index: Int,
    val recipeId: String?,
    val recipeName: String?,
    val recipeText: String? = null,
    val isEdited: Boolean = false
)

private enum class MealSelectionMode { RANDOM, MANUAL }

private fun MealSlot.toPlanMeal() = PlanMeal(
    recipeId = recipeId.orEmpty(),
    name = recipeName.orEmpty(),
    recipeText = recipeText.orEmpty()
)

/** One field of a callable's payload, which the functions SDK hands back as a raw map. */
private inline fun <reified T> Any?.field(name: String): T? =
    (this as? Map<*, *>)?.get(name) as? T

/** Plans aren't named anywhere in the flow yet, so they are stamped with the day instead. */
private fun defaultPlanName(): String {
    val today = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
    return "Meal plan - $today"
}

class GenerateViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    private val _mealCount = MutableStateFlow(7)
    val mealCount: StateFlow<Int> = _mealCount.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _mealSlots = MutableStateFlow<List<MealSlot>>(emptyList())
    val mealSlots: StateFlow<List<MealSlot>> = _mealSlots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadedPlanSlots = MutableStateFlow<List<MealSlot>>(emptyList())

    /** Whether the plan being edited still matches what was loaded from the DB. */
    val planHasChanges: StateFlow<Boolean> =
        combine(_mealSlots, _loadedPlanSlots) { slots, loaded -> slots != loaded }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _planName = MutableStateFlow("")
    val planName: StateFlow<String> = _planName.asStateFlow()

    private val _groceryList = MutableStateFlow("")
    val groceryList: StateFlow<String> = _groceryList.asStateFlow()

    private val _savedPlanName = MutableStateFlow("")
    private val _savedGroceryList = MutableStateFlow("")

    /**
     * Whether the grocery list screen holds anything the DB does not. A list that just came
     * back from the generator counts: it is on screen but unwritten until the user saves.
     */
    val groceryListHasChanges: StateFlow<Boolean> =
        combine(
            _planName,
            _groceryList,
            _savedPlanName,
            _savedGroceryList
        ) { name, list, savedName, savedList ->
            name.trim() != savedName || list.trim() != savedList
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Set while the cloud function is building a list, which freezes the editor. */
    private val _isGeneratingGroceryList = MutableStateFlow(false)
    val isGeneratingGroceryList: StateFlow<Boolean> = _isGeneratingGroceryList.asStateFlow()

    /** Whether this plan has been written to the DB, so further saves are updates. */
    private val _isPlanSaved = MutableStateFlow(false)
    val isPlanSaved: StateFlow<Boolean> = _isPlanSaved.asStateFlow()

    private val _selectableRecipes = MutableStateFlow<List<Recipe>>(emptyList())

    /** The recipes a slot can hold: everything the user hasn't archived, by name. */
    val selectableRecipes: StateFlow<List<Recipe>> = _selectableRecipes.asStateFlow()

    private var allRecipes: List<Recipe> = emptyList()
    private val availableRecipes: List<Recipe> get() = _selectableRecipes.value
    private var selectionMode = MealSelectionMode.RANDOM
    private var editingPlanId: String? = null

    fun setMealCount(count: Int) {
        _mealCount.value = count.coerceIn(MIN_MEALS, MAX_MEALS)
    }

    fun nextStep() {
        if (_currentStep.value < TOTAL_STEPS - 1) {
            _currentStep.value++
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    /**
     * Selects a random subset of the non-archived recipes of size [mealCount], then
     * navigates to step 2 with those meals. Recipes are unique unless there are fewer of
     * them than the meal count, in which case they repeat — spread as evenly as possible —
     * to fill every slot.
     */
    fun selectRandomMeals() {
        selectionMode = MealSelectionMode.RANDOM
        val count = _mealCount.value
        withRecipes {
            val available = availableRecipes
            val selected = if (available.isEmpty()) {
                emptyList()
            } else {
                buildList {
                    while (size < count) addAll(available.shuffled())
                }.take(count)
            }
            _mealSlots.value = selected.mapIndexed { i, recipe ->
                MealSlot(i, recipe.id, recipe.name, recipe.recipeText)
            } + (selected.size until count).map { i ->
                MealSlot(i, null, null)
            }
            _currentStep.value = 2
        }
    }

    /**
     * Navigates to step 2 with all meal slots unselected (manual selection).
     */
    fun selectManualMeals() {
        selectionMode = MealSelectionMode.MANUAL
        _mealSlots.value = (0 until _mealCount.value).map { i ->
            MealSlot(i, null, null)
        }
        _currentStep.value = 2
    }

    /**
     * Applies an edited title and body to this plan's copy of a meal. The saved recipe is
     * not touched; [isLocalEdit] is false when the same edit was written back to it, which
     * leaves the copy in step with the saved recipe.
     */
    fun updateMealSlotRecipe(
        index: Int,
        name: String,
        recipeText: String,
        isLocalEdit: Boolean = true
    ) {
        _mealSlots.value = _mealSlots.value.map { slot ->
            if (slot.index == index) {
                slot.copy(recipeName = name, recipeText = recipeText, isEdited = isLocalEdit)
            } else {
                slot
            }
        }
    }

    /**
     * Swaps one slot for another random recipe, preferring one the plan isn't already
     * using. Any edits made to that slot's copy are discarded.
     */
    fun rerollMealSlot(index: Int) {
        if (availableRecipes.isEmpty()) {
            withRecipes { applyReroll(index, availableRecipes) }
        } else {
            applyReroll(index, availableRecipes)
        }
    }

    /**
     * Points a slot at a recipe the user picked by hand, taking this plan's own fresh copy
     * of it. Any edits made to that slot's previous copy are discarded.
     */
    fun selectMealSlotRecipe(index: Int, recipeId: String) {
        val pick = _selectableRecipes.value.find { it.id == recipeId } ?: return
        _mealSlots.value = _mealSlots.value.map { slot ->
            if (slot.index == index) {
                MealSlot(index, pick.id, pick.name, pick.recipeText)
            } else {
                slot
            }
        }
    }

    /** Fills [selectableRecipes] for the picker, unless they are already cached. */
    fun loadSelectableRecipes() {
        if (_selectableRecipes.value.isEmpty()) withRecipes { }
    }

    /**
     * Appends a meal to the plan. In the random flow it arrives with a recipe the plan
     * isn't using yet; in the manual flow it arrives unselected.
     */
    fun addMealSlot() {
        val nextIndex = (_mealSlots.value.maxOfOrNull { it.index } ?: -1) + 1
        _mealSlots.value = _mealSlots.value + MealSlot(nextIndex, null, null)
        if (selectionMode == MealSelectionMode.RANDOM) {
            rerollMealSlot(nextIndex)
        }
    }

    /**
     * Drops a meal from the plan. The remaining slots keep their indices, which only ever
     * identify a slot.
     */
    fun removeMealSlot(index: Int) {
        _mealSlots.value = _mealSlots.value.filterNot { it.index == index }
    }

    private fun applyReroll(index: Int, available: List<Recipe>) {
        if (available.isEmpty()) return
        val slots = _mealSlots.value
        val slot = slots.find { it.index == index } ?: return
        val usedElsewhere = slots
            .filterNot { it.index == index }
            .mapNotNull { it.recipeId }
            .toSet()
        val notInPlan = available.filter { it.id !in usedElsewhere && it.id != slot.recipeId }
        val notCurrent = available.filter { it.id != slot.recipeId }
        val pick = notInPlan.ifEmpty { notCurrent }.ifEmpty { available }.random()
        _mealSlots.value = slots.map { existing ->
            if (existing.index == index) {
                MealSlot(index, pick.id, pick.name, pick.recipeText)
            } else {
                existing
            }
        }
    }

    /**
     * Loads a saved plan into the meal slots so it can be edited on its own, outside the
     * generate flow's earlier steps. Recipes the plan points at that no longer exist come
     * back unselected.
     *
     * A repeat call for the plan already loaded is ignored: the editing screen asks again
     * every time it is recomposed, including on the way back from editing one of its
     * meals, and re-reading the DB there would drop the edits made so far.
     */
    fun loadPlanForEdit(planId: String) {
        if (editingPlanId == planId) return
        editingPlanId = planId
        _isLoading.value = true
        firestore.collection(MEAL_PLANS).document(planId)
            .get()
            .addOnSuccessListener { doc ->
                val plan = doc.toObject(MealPlan::class.java)
                _planName.value = plan?.name.orEmpty()
                _groceryList.value = plan?.ingredientText.orEmpty()
                _savedPlanName.value = _planName.value.trim()
                _savedGroceryList.value = _groceryList.value.trim()
                _isPlanSaved.value = true
                val meals = plan?.meals ?: emptyList()
                val recipeIds = plan?.recipeIdsList ?: emptyList()
                withRecipes {
                    val slots = if (meals.isNotEmpty()) {
                        meals.mapIndexed { i, meal ->
                            MealSlot(
                                index = i,
                                recipeId = meal.recipeId.ifBlank { null },
                                recipeName = meal.name.ifBlank { null },
                                recipeText = meal.recipeText
                            )
                        }
                    } else {
                        recipeIds.mapIndexed { i, id ->
                            val recipe = allRecipes.find { it.id == id }
                            MealSlot(i, recipe?.id, recipe?.name, recipe?.recipeText)
                        }
                    }
                    _mealSlots.value = slots
                    _loadedPlanSlots.value = slots
                }
            }
            .addOnFailureListener {
                editingPlanId = null
                _isLoading.value = false
            }
    }

    /**
     * Writes the plan being built, then runs [onSaved]. The generate flow can reach this
     * more than once -- leaving the grocery list step drops back onto the meals, which can
     * be edited and saved again -- so everything after the first call updates the plan
     * created by that first call rather than adding another one.
     */
    fun savePlan(onSaved: () -> Unit) {
        if (editingPlanId != null) saveEditingPlan(onSaved) else createPlan(onSaved)
    }

    /**
     * Saves the meals built in the generate flow as a new plan, then runs [onSaved]. Each
     * meal is stored with the plan's own copy of its recipe as well as the recipe id.
     */
    private fun createPlan(onSaved: () -> Unit) {
        val user = firebaseAuth.currentUser ?: return
        val slots = _mealSlots.value.filter { it.recipeId != null }
        if (slots.isEmpty()) return
        val name = defaultPlanName()
        _isLoading.value = true
        firestore.collection(MEAL_PLANS)
            .add(
                MealPlanWrite(
                    name = name,
                    recipeIdsList = slots.mapNotNull { it.recipeId },
                    meals = slots.map { it.toPlanMeal() },
                    userId = user.uid
                )
            )
            .addOnSuccessListener { doc ->
                editingPlanId = doc.id
                _planName.value = name
                _savedPlanName.value = name
                _savedGroceryList.value = ""
                _loadedPlanSlots.value = _mealSlots.value
                _isPlanSaved.value = true
                _isLoading.value = false
                onSaved()
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    /**
     * Writes the edited meals back to the plan being edited, leaving its name and creation
     * time alone, then runs [onSaved].
     */
    fun saveEditingPlan(onSaved: () -> Unit) {
        val id = editingPlanId
        if (id.isNullOrBlank()) return
        val slots = _mealSlots.value.filter { it.recipeId != null }
        if (slots.isEmpty()) return
        _isLoading.value = true
        firestore.collection(MEAL_PLANS).document(id)
            .update(
                mapOf(
                    "recipeIdsList" to slots.mapNotNull { it.recipeId },
                    "meals" to slots.map { it.toPlanMeal() }
                )
            )
            .addOnSuccessListener {
                _loadedPlanSlots.value = _mealSlots.value
                _isLoading.value = false
                onSaved()
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun setPlanName(name: String) {
        _planName.value = name
    }

    fun setGroceryList(text: String) {
        _groceryList.value = text
    }

    /**
     * Sends the plan's meals to the generate-list cloud function and writes the list into
     * [groceryList] as it streams back, leaving the editor frozen until the stream ends.
     * Streaming is what `stream` buys over `call`: it sends the `Accept: text/event-stream`
     * the function keys its chunks off, so a `call` here silently gets one response at the
     * end no matter what the function does.
     *
     * A regenerate clears the old list on the way out and puts it back if the stream fails,
     * discarding whatever text had arrived: a dropped request shouldn't cost the user the
     * list they already had, or leave them one that stops halfway.
     */
    fun startGroceryListGeneration() {
        if (_isGeneratingGroceryList.value) return
        val meals = _mealSlots.value
            .filter { it.recipeId != null }
            .map {
                mapOf(
                    "name" to it.recipeName.orEmpty(),
                    "recipeText" to it.recipeText.orEmpty()
                )
            }
        if (meals.isEmpty()) return

        val previousList = _groceryList.value
        _isGeneratingGroceryList.value = true
        _groceryList.value = ""

        functions.getHttpsCallable(GENERATE_GROCERY_LIST)
            .withTimeout(GENERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .stream(mapOf("meals" to meals))
            .subscribe(object : Subscriber<StreamResponse> {
                override fun onSubscribe(subscription: Subscription) {
                    subscription.request(Long.MAX_VALUE)
                }

                override fun onNext(response: StreamResponse) {
                    when (response) {
                        is StreamResponse.Message -> {
                            val chunk = response.message.data.field<String>("text")
                            if (chunk != null) _groceryList.update { it + chunk }
                        }
                        // The last event carries the whole list, so the text assembled
                        // from the chunks gives way to it rather than adding to it.
                        is StreamResponse.Result -> {
                            val list = response.result.data.field<String>("groceryList")
                            if (list != null) _groceryList.value = list
                        }
                    }
                }

                override fun onError(error: Throwable) {
                    Log.e(TAG, "Grocery list generation failed", error)
                    _groceryList.value = previousList
                    _isGeneratingGroceryList.value = false
                }

                override fun onComplete() {
                    _isGeneratingGroceryList.value = false
                }
            })
    }

    /**
     * Writes the grocery list and the title it was edited under, then hands the plan's id
     * to [onSaved] so the caller can move on to the plan it belongs to.
     */
    fun saveGroceryList(onSaved: (planId: String) -> Unit) {
        val id = editingPlanId
        if (id.isNullOrBlank()) return
        _isLoading.value = true
        firestore.collection(MEAL_PLANS).document(id)
            .update(
                mapOf(
                    "name" to _planName.value.trim(),
                    "ingredientText" to _groceryList.value.trim()
                )
            )
            .addOnSuccessListener {
                _savedPlanName.value = _planName.value.trim()
                _savedGroceryList.value = _groceryList.value.trim()
                _isLoading.value = false
                onSaved(id)
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    /** Deletes the plan being edited, then runs [onDeleted]. */
    fun deleteEditingPlan(onDeleted: () -> Unit) {
        val id = editingPlanId
        if (id.isNullOrBlank()) return
        _isLoading.value = true
        firestore.collection(MEAL_PLANS).document(id)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                onDeleted()
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    /** Runs [onLoaded] once this user's recipes are cached, fetching them if needed. */
    private fun withRecipes(onLoaded: () -> Unit) {
        val user = firebaseAuth.currentUser ?: return
        _isLoading.value = true
        firestore.collection("recipes")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                allRecipes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.copy(id = doc.id)
                }
                _selectableRecipes.value = allRecipes
                    .filter { it.status != RecipeStatus.ARCHIVED }
                    .sortedBy { it.name.lowercase() }
                _isLoading.value = false
                onLoaded()
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    companion object {
        private const val MEAL_PLANS = "mealPlans"
        private const val GENERATE_GROCERY_LIST = "generateGroceryList"

        /**
         * Covers the whole stream, not just its first byte, so the SDK's 70s default would
         * cut a long generation off mid-list. Set past the function's own 120s deadline to
         * let the error it raises reach the client instead.
         */
        private const val GENERATION_TIMEOUT_SECONDS = 130L

        private const val TAG = "GenerateViewModel"
        private const val MIN_MEALS = 2
        private const val MAX_MEALS = 21
        const val TOTAL_STEPS = 4
    }
}
