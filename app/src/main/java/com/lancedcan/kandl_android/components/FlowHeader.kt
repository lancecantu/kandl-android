package com.lancedcan.kandl_android

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lancedcan.kandl_android.designsystem.Spacing
import com.lancedcan.kandl_android.designsystem.TouchTarget

/**
 * Centred screen title with an icon slot either side. Both slots hold their width whether
 * or not they are filled, so the title stays centred.
 */
@Composable
fun FlowHeader(
    title: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(TouchTarget.min),
            contentAlignment = Alignment.Center
        ) {
            leading?.invoke()
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Box(
            modifier = Modifier.width(TouchTarget.min),
            contentAlignment = Alignment.Center
        ) {
            trailing?.invoke()
        }
    }
}
