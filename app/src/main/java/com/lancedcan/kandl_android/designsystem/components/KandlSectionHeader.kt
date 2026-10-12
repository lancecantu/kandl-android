package com.lancedcan.kandl_android.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lancedcan.kandl_android.designsystem.BorderWidth
import com.lancedcan.kandl_android.designsystem.Spacing

/**
 * A label followed by a rule that runs out to the edge, splitting a list into sections.
 * [isFirst] drops the leading gap for the section that opens a list.
 */
@Composable
fun KandlSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isFirst) Modifier else Modifier.padding(top = Spacing.s5))
            .padding(bottom = Spacing.s2),
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
