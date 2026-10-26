package com.lancedcan.kandl_android.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Every padding, gap and inset in the app comes from this scale. */
object Spacing {
    val s1 = 2.dp
    val s2 = 4.dp
    val s3 = 8.dp
    val s4 = 12.dp
    val s5 = 16.dp
    val s6 = 24.dp
    val s7 = 36.dp
    val s8 = 48.dp
}

object CornerRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
}

object KandlShapes {
    val badge = RoundedCornerShape(CornerRadius.xs)
    val card = RoundedCornerShape(CornerRadius.sm)
    val listItem = RoundedCornerShape(CornerRadius.sm)
    val button = RoundedCornerShape(CornerRadius.md)
    val tile = RoundedCornerShape(CornerRadius.md)
    val pill = RoundedCornerShape(percent = 50)
}

object BorderWidth {
    val thin = 1.dp
}

/** Blur applied to content sitting behind a modal overlay. */
object BlurRadius {
    val overlay = 6.dp
}

object IconSize {
    val sm = 16.dp
    val md = 24.dp
    val lg = 32.dp
    val xl = 48.dp
}

/** Sizes the app logo renders at. The asset stays crisp up to [LogoSize.xl]. */
object LogoSize {
    val sm = 32.dp
    val md = 64.dp
    val lg = 96.dp
    val xl = 128.dp
}

/** Minimum touch target for icon-only controls. */
object TouchTarget {
    val min = 48.dp
}

/**
 * Fixed dimensions for specific components. Not part of the spacing scale — these are
 * intrinsic sizes, so they live here rather than as literals next to each component.
 */
object ComponentSize {
    val tileMax = 140.dp
    val wheelRow = 56.dp
    val textAreaMin = 200.dp

    /** Stroke of a spinner drawn at text size, where the Material default reads as a blob. */
    val inlineSpinnerStroke = 2.dp

    /** Height the grocery list preview is capped at before its text is clipped. */
    val groceryPreviewMax = 160.dp

    /** Depth of the gradient that fades clipped content out at an edge. */
    val fadeEdge = Spacing.s8

    /** Height of the action row inside a card; taller than the minimum touch target. */
    val cardActionRow = 56.dp

    /** Enough bottom inset to keep scrolled content clear of a FAB. */
    val fabClearance = Spacing.s8 + Spacing.s7

    /** Enough bottom inset to keep scrolled content clear of the pinned bottom bar. */
    val bottomActionClearance = Spacing.s8 + Spacing.s8
}
