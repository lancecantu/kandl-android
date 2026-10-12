package com.lancedcan.kandl_android.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Softens the bottom edge of clipped content into [color], so a hard cut-off reads as
 * "there is more below" rather than a rendering glitch.
 */
fun Modifier.fadeBottomEdge(
    color: Color,
    height: Dp = ComponentSize.fadeEdge
): Modifier = drawWithContent {
    drawContent()
    val fadeHeight = height.toPx().coerceAtMost(size.height)
    val top = size.height - fadeHeight
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0f), color),
            startY = top,
            endY = size.height
        ),
        topLeft = Offset(0f, top),
        size = Size(size.width, fadeHeight)
    )
}
