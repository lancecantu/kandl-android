package com.lancedcan.kandl_android

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import com.lancedcan.kandl_android.designsystem.ComponentSize
import com.lancedcan.kandl_android.designsystem.IconSize
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlIcon
import com.lancedcan.kandl_android.designsystem.components.KandlPrimaryButton
import com.lancedcan.kandl_android.designsystem.components.KandlSecondaryButton
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size

private const val MIN_MEALS = 2
private const val MAX_MEALS = 21
private val MEAL_COUNT_RANGE = MIN_MEALS..MAX_MEALS

private const val STEP_ANIMATION_DURATION_MS = 350

private const val STEP_MEALS = 2
private const val STEP_GROCERY_LIST = 3

@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel,
    onBack: () -> Unit,
    onEditMeal: (Int) -> Unit,
    onOpenGroceryList: () -> Unit
) {
    val mealCount by viewModel.mealCount.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val mealSlots by viewModel.mealSlots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPlanSaved by viewModel.isPlanSaved.collectAsState()
    val selectableRecipes by viewModel.selectableRecipes.collectAsState()
    var expandedMealIndex by remember { mutableStateOf<Int?>(null) }
    val showContinue = currentStep != 1 && currentStep != STEP_GROCERY_LIST
    val missingMeals = currentStep == STEP_MEALS && mealSlots.any { it.recipeName == null }

    LaunchedEffect(currentStep) {
        if (currentStep == STEP_MEALS) expandedMealIndex = null
    }

    val continueButtonAlpha by animateFloatAsState(
        targetValue = if (showContinue) 1f else 0f,
        animationSpec = tween(STEP_ANIMATION_DURATION_MS),
        label = "continueButtonAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlowHeader(
                title = "New meal plan",
                leading = {
                    if (currentStep > 0) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
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
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            // Forward: new content slides up from bottom, old slides up and out
                            slideInVertically(
                                animationSpec = tween(STEP_ANIMATION_DURATION_MS),
                                initialOffsetY = { fullHeight -> fullHeight }
                            ) togetherWith slideOutVertically(
                                animationSpec = tween(STEP_ANIMATION_DURATION_MS),
                                targetOffsetY = { fullHeight -> -fullHeight }
                            )
                        } else {
                            // Back: new content slides down from top, old slides down and out
                            slideInVertically(
                                animationSpec = tween(STEP_ANIMATION_DURATION_MS),
                                initialOffsetY = { fullHeight -> -fullHeight }
                            ) togetherWith slideOutVertically(
                                animationSpec = tween(STEP_ANIMATION_DURATION_MS),
                                targetOffsetY = { fullHeight -> fullHeight }
                            )
                        }
                    },
                    label = "stepContent"
                ) { step ->
                    when (step) {
                        0 -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.s6),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.s6)
                            ) {
                                Text(
                                    text = "How many meals? (you can add always more later)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                NumberedSelectionWheel(
                                    value = mealCount,
                                    onValueChange = { viewModel.setMealCount(it) },
                                    range = MEAL_COUNT_RANGE,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "$mealCount meals",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        1 -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.s6),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.s6)
                            ) {
                                Text(
                                    text = "How you would like the meals to be selected?",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.s6)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            onClick = { viewModel.selectRandomMeals() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .heightIn(max = ComponentSize.tileMax),
                                            shape = KandlShapes.tile
                                        ) {
                                            Icon(
                                                Icons.Default.Casino,
                                                contentDescription = null,
                                                modifier = Modifier.size(IconSize.xl)
                                            )
                                        }
                                        Text(
                                            text = "Random",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            onClick = { viewModel.selectManualMeals() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .heightIn(max = ComponentSize.tileMax),
                                            shape = KandlShapes.tile
                                        ) {
                                            Icon(
                                                Icons.Default.Assignment,
                                                contentDescription = null,
                                                modifier = Modifier.size(IconSize.xl)
                                            )
                                        }
                                        Text(
                                            text = "Manual",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        STEP_MEALS -> MealPlanMealList(
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
                            }
                        )
                        STEP_GROCERY_LIST -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.s6),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.s4)
                            ) {
                                KandlIcon(
                                    icon = Icons.AutoMirrored.Filled.ListAlt,
                                    contentDescription = null,
                                    size = IconSize.xl,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(Spacing.s3))
                                KandlPrimaryButton(
                                    text = "Generate grocery list",
                                    onClick = { viewModel.savePlan(onSaved = onOpenGroceryList) }
                                )
                                KandlSecondaryButton(
                                    text = "Save & continue",
                                    onClick = { viewModel.savePlan(onSaved = onBack) }
                                )
                            }
                        }
                        else -> {}
                    }
                }
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

        BottomActionBar(
            title = when {
                missingMeals -> "Select or remove missing meals"
                currentStep == STEP_MEALS && isPlanSaved -> "Save & continue"
                else -> "Continue"
            },
            onClick = {
                if (currentStep == STEP_MEALS && isPlanSaved) {
                    viewModel.savePlan(onSaved = { viewModel.nextStep() })
                } else {
                    viewModel.nextStep()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(continueButtonAlpha),
            enabled = showContinue && !missingMeals
        )
    }
}
