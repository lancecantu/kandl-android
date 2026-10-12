package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import com.lancedcan.kandl_android.designsystem.BlurRadius
import com.lancedcan.kandl_android.designsystem.ComponentSize
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlPillButton
import com.lancedcan.kandl_android.designsystem.components.KandlPrimaryButton
import com.lancedcan.kandl_android.designsystem.components.KandlSaveFab
import com.lancedcan.kandl_android.designsystem.components.KandlTextButton
import androidx.activity.compose.BackHandler

private const val OVERLAY_SCRIM_ALPHA = 0.12f

enum class RecipeEditMode {
    /** Editing a saved recipe, or creating one, from the Recipes tab. */
    LIBRARY,

    /** Editing the copy of a recipe that belongs to a meal plan being built. */
    MEAL_PLAN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    recipeId: String?,
    viewModel: RecipeManagerViewModel,
    onBack: () -> Unit,
    mode: RecipeEditMode = RecipeEditMode.LIBRARY,
    initialTitle: String = "",
    initialRecipeText: String = "",
    onSaveToMealPlan: (name: String, recipeText: String, isLocalEdit: Boolean) -> Unit =
        { _, _, _ -> }
) {
    val editTitle by viewModel.editTitle.collectAsState()
    val editRecipeText by viewModel.editRecipeText.collectAsState()
    val editRecipeStatus by viewModel.editRecipeStatus.collectAsState()
    val originalEditTitle by viewModel.originalEditTitle.collectAsState()
    val originalEditRecipeText by viewModel.originalEditRecipeText.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showSaveOptions by remember { mutableStateOf(false) }
    var showUpdateOriginalConfirm by remember { mutableStateOf(false) }

    val isMealPlanEdit = mode == RecipeEditMode.MEAL_PLAN

    val hasUnsavedChanges = if (recipeId == null && !isMealPlanEdit) {
        editTitle.isNotBlank() || editRecipeText.isNotBlank()
    } else {
        editTitle != originalEditTitle || editRecipeText != originalEditRecipeText
    }

    BackHandler(enabled = showSaveOptions || (hasUnsavedChanges && !showLeaveConfirm)) {
        if (showSaveOptions) showSaveOptions = false else showLeaveConfirm = true
    }

    LaunchedEffect(recipeId, mode) {
        when {
            isMealPlanEdit -> viewModel.startEditingMealPlanCopy(
                recipeId = recipeId,
                name = initialTitle,
                recipeText = initialRecipeText
            )
            recipeId == null -> viewModel.startNewRecipe()
            else -> viewModel.loadRecipeForEdit(recipeId)
        }
    }

    val saveToMealPlan = { isLocalEdit: Boolean ->
        onSaveToMealPlan(editTitle.trim(), editRecipeText.trim(), isLocalEdit)
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (showSaveOptions) Modifier.blur(BlurRadius.overlay) else Modifier,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (hasUnsavedChanges) showLeaveConfirm = true else onBack()
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (recipeId != null && !isMealPlanEdit) {
                            IconButton(
                                onClick = {
                                    viewModel.updateRecipeStatus(
                                        if (editRecipeStatus == RecipeStatus.FAVORITED) RecipeStatus.DEFAULT
                                        else RecipeStatus.FAVORITED
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = if (editRecipeStatus == RecipeStatus.FAVORITED)
                                        Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = if (editRecipeStatus == RecipeStatus.FAVORITED)
                                        "Unfavorite" else "Favorite"
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.updateRecipeStatus(
                                        if (editRecipeStatus == RecipeStatus.ARCHIVED) RecipeStatus.DEFAULT
                                        else RecipeStatus.ARCHIVED
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = if (editRecipeStatus == RecipeStatus.ARCHIVED)
                                        Icons.Filled.Archive else Icons.Outlined.Archive,
                                    contentDescription = if (editRecipeStatus == RecipeStatus.ARCHIVED)
                                        "Unarchive" else "Archive"
                                )
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete recipe"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.s6)
                        .padding(top = Spacing.s3, bottom = ComponentSize.fabClearance)
                ) {
                    BasicTextField(
                        value = editTitle,
                        onValueChange = { viewModel.setEditTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.s4)
                            ) {
                                if (editTitle.isEmpty()) {
                                    Text(
                                        "Recipe title...",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(Spacing.s5))

                    BasicTextField(
                        value = editRecipeText,
                        onValueChange = { viewModel.setEditRecipeText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = ComponentSize.textAreaMin),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.s4)
                            ) {
                                if (editRecipeText.isEmpty()) {
                                    Text(
                                        "Write out your Ingredients...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                val canSave = editTitle.isNotBlank() && editRecipeText.isNotBlank()
                val showSaveButton = canSave && (recipeId == null || hasUnsavedChanges)

                if (showSaveButton && !showSaveOptions) {
                    KandlSaveFab(
                        onClick = {
                            if (isMealPlanEdit) {
                                showSaveOptions = true
                            } else {
                                viewModel.saveRecipe()
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(Spacing.s6)
                    )
                }
            }
        }

        if (showSaveOptions) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = OVERLAY_SCRIM_ALPHA))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showSaveOptions = false }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.s6),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
                ) {
                    KandlPillButton(
                        text = "Save to plan only",
                        onClick = { saveToMealPlan(true) }
                    )
                    if (recipeId != null) {
                        KandlPillButton(
                            text = "Save to plan & update original recipe",
                            onClick = { showUpdateOriginalConfirm = true }
                        )
                    }
                    KandlSaveFab(onClick = { showSaveOptions = false })
                }
            }
        }
    }

    if (showUpdateOriginalConfirm) {
        AlertDialog(
            onDismissRequest = { showUpdateOriginalConfirm = false },
            title = {
                Text(
                    "Update confirmation",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "This will update the original recipe, are you sure you would like to continue?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                KandlPrimaryButton(
                    text = "Continue",
                    onClick = {
                        viewModel.updateOriginalRecipe()
                        showUpdateOriginalConfirm = false
                        saveToMealPlan(false)
                    },
                    modifier = Modifier
                )
            },
            dismissButton = {
                KandlTextButton(
                    text = "Cancel",
                    onClick = { showUpdateOriginalConfirm = false },
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete recipe?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recipeId?.let { viewModel.deleteRecipe(it) }
                        showDeleteConfirm = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = {
                Text(
                    "Are you sure you want to leave?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "Your changes will be lost.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLeaveConfirm = false }
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

