package com.lancedcan.kandl_android

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// No-op stand-in so non-QA builds never compile the dev menu. Real one lives in src/qa.
@Composable
@Suppress("UNUSED_PARAMETER")
fun DevMenuOverlay(modifier: Modifier = Modifier) {
}
