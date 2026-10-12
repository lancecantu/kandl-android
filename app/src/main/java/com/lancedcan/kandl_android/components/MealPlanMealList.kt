package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.ComponentSize
import com.lancedcan.kandl_android.designsystem.IconSize
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlIcon
import com.lancedcan.kandl_android.designsystem.components.KandlPrimaryButton
import com.lancedcan.kandl_android.designsystem.components.KandlTextButton

private const val DISABLED_ICON_ALPHA = 0.38f

/**
 * A meal action that would throw away edits made to this plan's copy of the recipe,
 * along with the wording used to check the user meant it.
 */
private enum class MealCardConfirm(val title: String, val message: String) {
    SELECT(
        "Select confirmation",
        "You've made manual updates to this recipe. Pick a different one anyway?"
    ),
    RANDOMIZE(
        "Randomize confirmation",
        "You've made manual updates to this recipe. Select a new random one anyway?"
    ),
    REMOVE(
        "Remove confirmation",
        "You've made manual updates to this recipe. Remove this meal anyway?"
    )
}

/**
 * The meals of a plan, each expandable to edit, pick, re-roll or drop it, above an action
 * that appends another meal. Shared by the generate flow and by editing a saved plan,
 * which fills [header] with the grocery list that sits above the meals.
 */
@Composable
fun MealPlanMealList(
    mealSlots: List<MealSlot>,
    expandedIndex: Int?,
    onToggleExpanded: (Int) -> Unit,
    onEditMeal: (Int) -> Unit,
    onRandomizeMeal: (Int) -> Unit,
    onRemoveMeal: (Int) -> Unit,
    onAddMeal: () -> Unit,
    recipeOptions: List<Recipe>,
    isLoadingRecipeOptions: Boolean,
    onRequestRecipeOptions: () -> Unit,
    onSelectRecipe: (Int, Recipe) -> Unit,
    header: (@Composable ColumnScope.() -> Unit)? = null
) {
    var pickerSlotIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.s6)
            .padding(top = Spacing.s6, bottom = ComponentSize.bottomActionClearance),
        verticalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        header?.invoke(this)
        PrimaryActionCard(
            title = "Add meal +",
            onClick = onAddMeal
        )
        mealSlots.forEach { slot ->
            MealPlanMealCard(
                slot = slot,
                isExpanded = expandedIndex == slot.index,
                onToggleExpanded = { onToggleExpanded(slot.index) },
                onEditRecipe = { onEditMeal(slot.index) },
                onSelectRecipe = {
                    onRequestRecipeOptions()
                    pickerSlotIndex = slot.index
                },
                onRandomize = { onRandomizeMeal(slot.index) },
                onRemove = { onRemoveMeal(slot.index) },
                canRemove = mealSlots.size > 1
            )
        }
    }

    val pickerSlot = mealSlots.find { it.index == pickerSlotIndex }
    if (pickerSlot != null) {
        RecipeSelectSheet(
            recipes = recipeOptions,
            isLoading = isLoadingRecipeOptions,
            selectedRecipeId = pickerSlot.recipeId,
            onSelect = { recipe ->
                onSelectRecipe(pickerSlot.index, recipe)
                pickerSlotIndex = null
            },
            onDismiss = { pickerSlotIndex = null }
        )
    }
}

/** One cell of a meal card's action row, tappable across its whole width. */
@Composable
private fun MealCardAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = Spacing.s3)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KandlIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                null
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ICON_ALPHA)
            }
        )
    }
}

@Composable
private fun MealCardActionDivider() {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = Spacing.s3),
        thickness = BorderWidth.thin,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun MealPlanMealCard(
    slot: MealSlot,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEditRecipe: () -> Unit,
    onSelectRecipe: () -> Unit,
    onRandomize: () -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    val label = slot.recipeName ?: "UNSELECTED MEAL"
    val isUnselected = slot.recipeName == null
    var pendingConfirm by remember { mutableStateOf<MealCardConfirm?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = BorderWidth.thin,
                color = MaterialTheme.colorScheme.outline,
                shape = KandlShapes.card
            )
            .clickable(onClick = onToggleExpanded)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.s5)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isUnselected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ComponentSize.cardActionRow),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MealCardAction(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit recipe",
                    onClick = onEditRecipe,
                    modifier = Modifier.weight(1f),
                    enabled = !isUnselected
                )
                MealCardActionDivider()
                MealCardAction(
                    icon = Icons.Default.Assignment,
                    contentDescription = "Select recipe",
                    onClick = {
                        if (slot.isEdited) {
                            pendingConfirm = MealCardConfirm.SELECT
                        } else {
                            onSelectRecipe()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                MealCardActionDivider()
                MealCardAction(
                    icon = Icons.Default.Casino,
                    contentDescription = "Randomize",
                    onClick = {
                        if (slot.isEdited) {
                            pendingConfirm = MealCardConfirm.RANDOMIZE
                        } else {
                            onRandomize()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                MealCardActionDivider()
                MealCardAction(
                    icon = Icons.Default.Delete,
                    contentDescription = "Remove",
                    onClick = {
                        if (slot.isEdited) {
                            pendingConfirm = MealCardConfirm.REMOVE
                        } else {
                            onRemove()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = canRemove
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.s5)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(IconSize.lg)
            )
        }
    }

    pendingConfirm?.let { confirm ->
        AlertDialog(
            onDismissRequest = { pendingConfirm = null },
            title = {
                Text(
                    confirm.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    confirm.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                KandlPrimaryButton(
                    text = "Continue",
                    onClick = {
                        pendingConfirm = null
                        when (confirm) {
                            MealCardConfirm.SELECT -> onSelectRecipe()
                            MealCardConfirm.RANDOMIZE -> onRandomize()
                            MealCardConfirm.REMOVE -> onRemove()
                        }
                    },
                    modifier = Modifier
                )
            },
            dismissButton = {
                KandlTextButton(
                    text = "Cancel",
                    onClick = { pendingConfirm = null },
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
