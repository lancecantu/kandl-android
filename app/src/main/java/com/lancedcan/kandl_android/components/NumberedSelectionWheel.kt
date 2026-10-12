package com.lancedcan.kandl_android

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lancedcan.kandl_android.designsystem.ComponentSize
import kotlin.math.abs

private val ROW_HEIGHT = ComponentSize.wheelRow
private const val VISIBLE_ROWS = 5

/**
 * Wheel-style number picker: scroll vertically to select a value; the list snaps so the
 * selected item is centered. No image assets—drawn entirely in Compose.
 */
@Composable
fun NumberedSelectionWheel(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    // Index 0 = leading spacer; number items start at index 1
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (value - range.first + 1).coerceIn(1, range.count())
    )
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportHeight = layoutInfo.viewportSize.height
            val centerY = viewportHeight / 2f
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf range.first
            val centerItem = visibleItems.minByOrNull { item ->
                val itemCenterY = item.offset + item.size / 2
                abs(itemCenterY - centerY)
            } ?: visibleItems.first()
            // Index 0 is the leading spacer; number items start at index 1
            val indexInList = (centerItem.index - 1).coerceAtLeast(0)
            (range.first + indexInList).coerceIn(range)
        }
    }

    LaunchedEffect(selectedIndex) {
        onValueChange(selectedIndex)
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(ROW_HEIGHT * VISIBLE_ROWS)
                .wheelFadeEdges(surfaceColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = flingBehavior
        ) {
            item {
                Spacer(modifier = Modifier.height(ROW_HEIGHT * 2))
            }
            itemsIndexed(range.toList(), key = { _, number -> number }) { index, number ->
                val layoutInfo = listState.layoutInfo
                val viewportHeight = layoutInfo.viewportSize.height
                // Index 0 = leading spacer; number items start at list index 1
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index + 1 }
                val alpha = if (itemInfo != null) {
                    val itemCenterY = itemInfo.offset + itemInfo.size / 2f
                    val centerY = viewportHeight / 2f
                    val distance = abs(itemCenterY - centerY)
                    val fadeStart = viewportHeight * 0.15f
                    (1f - ((distance - fadeStart) / (viewportHeight / 2f)).coerceIn(0f, 1f))
                } else 0.3f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ROW_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(ROW_HEIGHT * 2))
            }
        }
    }
}

private fun Modifier.wheelFadeEdges(surfaceColor: Color): Modifier = drawWithContent {
    val height = size.height
    drawContent()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                surfaceColor,
                surfaceColor.copy(alpha = 0f)
            ),
            startY = 0f,
            endY = height * 0.35f
        )
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                surfaceColor.copy(alpha = 0f),
                surfaceColor
            ),
            startY = height * 0.65f,
            endY = height
        )
    )
}
