package com.lancedcan.kandl_android.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.lancedcan.kandl_android.R
import com.lancedcan.kandl_android.designsystem.LogoSize

enum class KandlLogoShape { RoundedSquare, Circle }

/**
 * The app logo. Each [shape] is its own artwork rather than a clip of the other —
 * the mark runs close enough to the edge that clipping a rounded square into a
 * circle would cut the knife and the fork tines. Leave [contentDescription] null
 * wherever the logo is decorative.
 */
@Composable
fun KandlLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = LogoSize.md,
    shape: KandlLogoShape = KandlLogoShape.RoundedSquare
) {
    val logo = when (shape) {
        KandlLogoShape.RoundedSquare -> R.drawable.ic_kandl_logo
        KandlLogoShape.Circle -> R.drawable.ic_kandl_logo_circle
    }
    Image(
        painter = painterResource(logo),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
