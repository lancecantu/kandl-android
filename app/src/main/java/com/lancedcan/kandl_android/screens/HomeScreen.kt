package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.Spacing

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s5, bottom = Spacing.s2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(Spacing.s3))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = BorderWidth.thin,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = HomeViewModel(),
    onProfileClick: () -> Unit = {},
    onGenerateClick: () -> Unit = {},
    onPlanClick: (String) -> Unit = {}
) {
    val mealPlans by viewModel.mealPlans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMealPlans()
    }

    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScreenTitleHeader(
                title = "Meal plans",
                modifier = Modifier.padding(start = Spacing.s6, top = Spacing.s6, end = Spacing.s6),
                trailing = { ProfileButton(onClick = onProfileClick) }
            )
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
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScreenTitleHeader(
                title = "Meal plans",
                modifier = Modifier.padding(start = Spacing.s6, top = Spacing.s6, end = Spacing.s6),
                trailing = { ProfileButton(onClick = onProfileClick) }
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(Spacing.s6),
                verticalArrangement = Arrangement.spacedBy(Spacing.s3)
            ) {
                item {
                    PrimaryActionCard(
                        title = "Generate Now +",
                        onClick = onGenerateClick
                    )
                }
                item {
                    SectionHeader("Past meal plans")
                }
                items(mealPlans, key = { it.id ?: "" }) { plan ->
                    MealPlanCard(
                        name = plan.name,
                        mealCount = plan.recipeIdsList.size,
                        onClick = { plan.id?.let(onPlanClick) }
                    )
                }
            }
        }
    }
}