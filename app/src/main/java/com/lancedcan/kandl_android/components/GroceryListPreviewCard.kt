package com.lancedcan.kandl_android

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.ComponentSize
import com.lancedcan.kandl_android.designsystem.KandlShapes
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.fadeBottomEdge

/**
 * The top of a saved plan's grocery list, capped in height so it stays a preview. When
 * the plan has no list yet it becomes the prompt that goes off and generates one.
 */
@Composable
fun GroceryListPreviewCard(
    groceryList: String,
    onOpen: () -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (groceryList.isBlank()) {
        PrimaryActionCard(
            title = "Generate grocery list",
            onClick = onGenerate,
            modifier = modifier
        )
        return
    }

    var isClipped by remember(groceryList) { mutableStateOf(false) }
    val fadeColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = ComponentSize.groceryPreviewMax)
            .border(
                width = BorderWidth.thin,
                color = MaterialTheme.colorScheme.outline,
                shape = KandlShapes.card
            )
            .clipToBounds()
            .clickable(onClick = onOpen)
            .then(if (isClipped) Modifier.fadeBottomEdge(fadeColor) else Modifier)
            .padding(start = Spacing.s5, end = Spacing.s5, top = Spacing.s5)
    ) {
        Column {
            Text(
                text = groceryList,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                onTextLayout = { isClipped = it.hasVisualOverflow }
            )
            Spacer(modifier = Modifier.height(Spacing.s5))
        }
    }
}
