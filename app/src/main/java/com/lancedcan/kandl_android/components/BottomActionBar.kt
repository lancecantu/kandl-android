package com.lancedcan.kandl_android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.Spacing

/**
 * A screen's primary action, pinned to the bottom on an opaque surface that the content
 * behind it scrolls under. Pair with [com.lancedcan.kandl_android.designsystem.ComponentSize.bottomActionClearance]
 * as the bottom inset of that content.
 */
@Composable
fun BottomActionBar(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.s6)
            .padding(top = Spacing.s4, bottom = Spacing.s6),
        contentAlignment = Alignment.Center
    ) {
        PrimaryActionCard(
            title = title,
            onClick = onClick,
            enabled = enabled
        )
    }
}
