package com.lancedcan.kandl_android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import com.lancedcan.kandl_android.designsystem.ComponentSize
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlInlineSpinner
import com.lancedcan.kandl_android.designsystem.components.KandlPrimaryButton
import com.lancedcan.kandl_android.designsystem.components.KandlSaveFab
import com.lancedcan.kandl_android.designsystem.components.KandlTextButton

private const val PLACEHOLDER_ALPHA = 0.6f

/**
 * A plan's grocery list as a notepad, carrying over the title the plan was saved under.
 * Both fields are locked while a generation is in flight.
 *
 * [generateOnOpen] covers the two entries that ask for a list rather than open the one
 * already saved: continuing out of the generate flow, and the prompt on a saved plan that
 * has no list yet.
 *
 * Saving is a step forward: [onContinue] decides where a written list lands, while [onBack]
 * is only for leaving one behind.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    viewModel: GenerateViewModel,
    onBack: () -> Unit,
    onContinue: (planId: String) -> Unit,
    generateOnOpen: Boolean = false
) {
    val planName by viewModel.planName.collectAsState()
    val groceryList by viewModel.groceryList.collectAsState()
    val isGenerating by viewModel.isGeneratingGroceryList.collectAsState()
    val hasUnsavedChanges by viewModel.groceryListHasChanges.collectAsState()
    var showRegenerateConfirm by remember { mutableStateOf(false) }
    var showSavePrompt by remember { mutableStateOf(false) }

    LaunchedEffect(generateOnOpen) {
        if (generateOnOpen) viewModel.startGroceryListGeneration()
    }

    BackHandler(enabled = isGenerating || (hasUnsavedChanges && !showSavePrompt)) {
        if (!isGenerating) showSavePrompt = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) showSavePrompt = true else onBack()
                        },
                        enabled = !isGenerating
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (groceryList.isBlank()) {
                                viewModel.startGroceryListGeneration()
                            } else {
                                showRegenerateConfirm = true
                            }
                        },
                        enabled = !isGenerating
                    ) {
                        Icon(
                            Icons.Filled.Autorenew,
                            contentDescription = "Regenerate grocery list"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
                    value = planName,
                    onValueChange = { viewModel.setPlanName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
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
                            if (planName.isEmpty()) {
                                Text(
                                    "Meal plan title...",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                        .copy(alpha = PLACEHOLDER_ALPHA)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.s5))

                BasicTextField(
                    value = groceryList,
                    onValueChange = { viewModel.setGroceryList(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = ComponentSize.textAreaMin),
                    enabled = !isGenerating,
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
                            if (groceryList.isEmpty()) {
                                val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = PLACEHOLDER_ALPHA)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
                                ) {
                                    Text(
                                        text = if (isGenerating) {
                                            "Building your grocery list..."
                                        } else {
                                            "Your grocery list will appear here..."
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = placeholderColor
                                    )
                                    if (isGenerating) {
                                        KandlInlineSpinner(color = placeholderColor)
                                    }
                                }
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (!isGenerating) {
                KandlSaveFab(
                    onClick = { viewModel.saveGroceryList(onSaved = onContinue) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.s6)
                )
            }
        }
    }

    if (showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = { showRegenerateConfirm = false },
            title = {
                Text(
                    "Regenerate grocery list?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "This will overwrite the existing grocery list",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                KandlPrimaryButton(
                    text = "Yes, continue",
                    onClick = {
                        showRegenerateConfirm = false
                        viewModel.startGroceryListGeneration()
                    },
                    modifier = Modifier
                )
            },
            dismissButton = {
                KandlTextButton(
                    text = "Cancel",
                    onClick = { showRegenerateConfirm = false },
                    contentColor = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    if (showSavePrompt) {
        SaveChangesDialog(
            onExit = {
                showSavePrompt = false
                onBack()
            },
            onSaveAndExit = {
                showSavePrompt = false
                viewModel.saveGroceryList(onSaved = onContinue)
            },
            onDismiss = { showSavePrompt = false }
        )
    }
}
