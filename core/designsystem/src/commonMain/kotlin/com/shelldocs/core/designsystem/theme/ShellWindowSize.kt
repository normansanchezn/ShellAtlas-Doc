package com.shelldocs.core.designsystem.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared width breakpoints (dp) for adaptive layout decisions across desktop/web/tablet/mobile.
 * Mirrors the values previously duplicated in WorkspaceShell so feature modules can branch
 * on the same thresholds without depending on the app module.
 */
object ShellWindowSize {
    const val RAIL_MIN_WIDTH_DP = 600
    const val WIDE_MIN_WIDTH_DP = 840
    const val ULTRAWIDE_MIN_WIDTH_DP = 1440

    /** Readable max width for text-heavy content (editors, forms) on ultrawide desktop/web. */
    const val MAX_CONTENT_WIDTH_DP = 1120

    fun isRail(widthDp: Int): Boolean = widthDp in RAIL_MIN_WIDTH_DP until WIDE_MIN_WIDTH_DP
    fun isWide(widthDp: Int): Boolean = widthDp >= WIDE_MIN_WIDTH_DP
    fun isUltrawide(widthDp: Int): Boolean = widthDp >= ULTRAWIDE_MIN_WIDTH_DP
}

/** Caps a content column's width so it doesn't stretch unreadably wide on ultrawide desktop/web. */
fun Modifier.shellMaxContentWidth(): Modifier =
    this.widthIn(max = ShellWindowSize.MAX_CONTENT_WIDTH_DP.dp)
