package com.shelldocs.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * Shadow elevation scale. Three levels only — never pick a value outside this
 * set. `none` is for flat/bordered surfaces (default ShellCard), `raised` for
 * cards that float above content (metric cards, popovers), `overlay` for
 * dialogs/sheets/menus that sit above everything.
 */
object ShellElevation {
    val none = 0.dp
    val raised = 3.dp
    val overlay = 12.dp
}
