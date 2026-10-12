package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.Spacing

@Composable
fun MealPlanCard(
    name: String,
    mealCount: Int?,
    onClick: () -> Unit
) {
    val shape = KandlShapes.card
    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, shape)
            .border(
                width = BorderWidth.thin,
                color = borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(Spacing.s5),
        horizontalAlignment = if (mealCount == null) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (mealCount != null) {
            Text(
                text = if (mealCount == 1) "1 meal" else "$mealCount meals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
