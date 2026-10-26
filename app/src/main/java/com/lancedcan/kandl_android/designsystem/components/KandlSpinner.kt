package com.lancedcan.kandl_android.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import com.lancedcan.kandl_android.designsystem.ComponentSize

/**
 * A spinner sized to sit on a line of text. Its diameter is derived from [fontSize] rather
 * than an icon size so it keeps matching the text it sits next to at any font scale.
 */
@Composable
fun KandlInlineSpinner(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    color: Color = LocalContentColor.current
) {
    val diameter = with(LocalDensity.current) { fontSize.toDp() }
    CircularProgressIndicator(
        modifier = modifier.size(diameter),
        color = color,
        strokeWidth = ComponentSize.inlineSpinnerStroke,
        trackColor = Color.Transparent,
        strokeCap = StrokeCap.Round
    )
}
