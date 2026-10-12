package com.lancedcan.kandl_android.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * The only five text sizes in the app. Derived from the sizes that were already in use
 * (11, 12, 14, 16, 22, 24, 28); 11 snapped up to xs and 24 snapped down to l.
 */
object FontSize {
    val xs = 12.sp
    val s = 14.sp
    val m = 16.sp
    val l = 22.sp
    val xl = 28.sp
}

object LineHeight {
    val xs = 16.sp
    val s = 20.sp
    val m = 24.sp
    val l = 28.sp
    val xl = 36.sp
}

private object Tracking {
    val xs = 0.4.sp
    val s = 0.25.sp
    val m = 0.15.sp
    val l = 0.sp
    val xl = 0.sp
}

private fun style(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing
)

private val Xs = style(FontSize.xs, LineHeight.xs, Tracking.xs)
private val XsMedium = style(FontSize.xs, LineHeight.xs, Tracking.xs, FontWeight.Medium)
private val S = style(FontSize.s, LineHeight.s, Tracking.s)
private val SMedium = style(FontSize.s, LineHeight.s, Tracking.s, FontWeight.Medium)
private val M = style(FontSize.m, LineHeight.m, Tracking.m)
private val MMedium = style(FontSize.m, LineHeight.m, Tracking.m, FontWeight.Medium)
private val L = style(FontSize.l, LineHeight.l, Tracking.l)
private val Xl = style(FontSize.xl, LineHeight.xl, Tracking.xl)

/**
 * Every Material role is backed by one of the five sizes, so anything reading
 * `MaterialTheme.typography` — including Material's own components — stays on the scale.
 */
val Typography = Typography(
    displayLarge = Xl,
    displayMedium = Xl,
    displaySmall = Xl,
    headlineLarge = Xl,
    headlineMedium = Xl,
    headlineSmall = L,
    titleLarge = L,
    titleMedium = MMedium,
    titleSmall = SMedium,
    bodyLarge = M,
    bodyMedium = S,
    bodySmall = Xs,
    labelLarge = SMedium,
    labelMedium = XsMedium,
    labelSmall = XsMedium
)
