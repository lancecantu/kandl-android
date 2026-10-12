package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlIcon
import com.lancedcan.kandl_android.designsystem.components.KandlListItem

/**
 * Full-screen list of the recipes a meal can be set to, covering the screen it was opened
 * from. Recipe contents aren't shown here — tapping a row picks that recipe outright.
 */
@Composable
fun RecipeSelectSheet(
    recipes: List<Recipe>,
    isLoading: Boolean,
    selectedRecipeId: String?,
    onSelect: (Recipe) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FlowHeader(
                    title = "Select a recipe",
                    modifier = Modifier.padding(vertical = Spacing.s3),
                    trailing = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading && recipes.isEmpty() -> CircularProgressIndicator()
                        recipes.isEmpty() -> Text(
                            text = "No recipes to choose from",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.s6),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s3)
                        ) {
                            items(recipes, key = { it.id }) { recipe ->
                                KandlListItem(
                                    title = recipe.name,
                                    onClick = { onSelect(recipe) },
                                    trailing = if (recipe.id == selectedRecipeId) {
                                        {
                                            KandlIcon(
                                                icon = Icons.Default.Check,
                                                contentDescription = "Currently selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
