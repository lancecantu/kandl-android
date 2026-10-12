package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.components.KandlSectionHeader

@Composable
private fun RecipeItem(recipe: Recipe, onClick: () -> Unit) {
    val shape = KandlShapes.card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                width = BorderWidth.thin,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(Spacing.s5)
    ) {
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun RecipeListScreen(
    viewModel: RecipeManagerViewModel,
    onProfileClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val favoritedRecipes = recipes
        .filter { it.status == RecipeStatus.FAVORITED }
        .sortedBy { it.name.lowercase() }
    val defaultRecipes = recipes
        .filter { it.status == RecipeStatus.DEFAULT }
        .sortedBy { it.name.lowercase() }
    val archivedRecipes = recipes
        .filter { it.status == RecipeStatus.ARCHIVED }
        .sortedBy { it.name.lowercase() }

    LaunchedEffect(Unit) {
        viewModel.loadRecipes()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ScreenTitleHeader(
                title = "Recipes",
                modifier = Modifier.padding(start = Spacing.s6, top = Spacing.s6, end = Spacing.s6),
                trailing = { ProfileButton(onClick = onProfileClick) }
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(Spacing.s6),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (favoritedRecipes.isEmpty() && defaultRecipes.isEmpty() && archivedRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(Spacing.s6),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recipes yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(Spacing.s6),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s3)
                ) {
                    item {
                        KandlSectionHeader("Favorites", isFirst = true)
                    }
                    items(favoritedRecipes, key = { it.id }) { recipe ->
                        RecipeItem(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                    item {
                        KandlSectionHeader("All recipes")
                    }
                    items(defaultRecipes, key = { it.id }) { recipe ->
                        RecipeItem(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                    item {
                        KandlSectionHeader("Archived (won't be included in generation)")
                    }
                    items(archivedRecipes, key = { it.id }) { recipe ->
                        RecipeItem(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.startNewRecipe()
                onAddRecipeClick()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.s6),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add recipe")
        }
    }
}
