package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.lancedcan.kandl_android.designsystem.components.KandlPrimaryButton
import com.lancedcan.kandl_android.designsystem.components.KandlSectionHeader
import com.lancedcan.kandl_android.designsystem.components.KandlTextButton

/**
 * A saved plan's meals, on their own. Same list and per-meal actions as the last step of
 * the generate flow, but with no way back into the earlier steps — the leading action
 * deletes the plan instead.
 */
@Composable
fun MealPlanEditScreen(
    planId: String,
    viewModel: GenerateViewModel,
    onBack: () -> Unit,
    onEditMeal: (Int) -> Unit,
    onOpenGroceryList: (generate: Boolean) -> Unit
) {
    val mealSlots by viewModel.mealSlots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val planName by viewModel.planName.collectAsState()
    val groceryList by viewModel.groceryList.collectAsState()
    val planHasChanges by viewModel.planHasChanges.collectAsState()
    val selectableRecipes by viewModel.selectableRecipes.collectAsState()
    var expandedMealIndex by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val missingMeals = mealSlots.any { it.recipeName == null }
    val showSaveAction = planHasChanges || missingMeals

    LaunchedEffect(planId) {
        viewModel.loadPlanForEdit(planId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlowHeader(
                title = planName.ifBlank { "Meal plan" },
                leading = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete meal plan")
                    }
                },
                trailing = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MealPlanMealList(
                    mealSlots = mealSlots,
                    expandedIndex = expandedMealIndex,
                    onToggleExpanded = { index ->
                        expandedMealIndex = if (expandedMealIndex == index) null else index
                    },
                    onEditMeal = onEditMeal,
                    onRandomizeMeal = { index -> viewModel.rerollMealSlot(index) },
                    onRemoveMeal = { index -> viewModel.removeMealSlot(index) },
                    onAddMeal = { viewModel.addMealSlot() },
                    recipeOptions = selectableRecipes,
                    isLoadingRecipeOptions = isLoading,
                    onRequestRecipeOptions = { viewModel.loadSelectableRecipes() },
                    onSelectRecipe = { index, recipe ->
                        viewModel.selectMealSlotRecipe(index, recipe.id)
                    },
                    header = {
                        KandlSectionHeader("Grocery list", isFirst = true)
                        GroceryListPreviewCard(
                            groceryList = groceryList,
                            onOpen = { onOpenGroceryList(false) },
                            onGenerate = { onOpenGroceryList(true) }
                        )
                        KandlSectionHeader("Meals")
                    }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (showSaveAction) {
            BottomActionBar(
                title = if (missingMeals) "Select or remove missing meals" else "Save updates",
                onClick = { viewModel.saveEditingPlan(onSaved = onBack) },
                modifier = Modifier.align(Alignment.BottomCenter),
                enabled = !missingMeals
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    "Delete confirmation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "This will delete this meal plan, are you sure you would like to continue?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                KandlPrimaryButton(
                    text = "Continue",
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteEditingPlan(onDeleted = onBack)
                    },
                    modifier = Modifier
                )
            },
            dismissButton = {
                KandlTextButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirm = false },
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
