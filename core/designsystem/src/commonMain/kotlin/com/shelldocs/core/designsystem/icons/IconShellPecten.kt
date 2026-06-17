package com.shelldocs.core.designsystem.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/** Simplified Shell pecten (scallop) mark used in the app logo chip. */
val IconShellPecten: ImageVector by lazy {
    shellIcon("IconShellPecten") {
        moveTo(12f, 21f)
        curveTo(7f, 21f, 3f, 17f, 3f, 12f)
        curveTo(3f, 7f, 7f, 3.5f, 12f, 3.5f)
        curveTo(17f, 3.5f, 21f, 7f, 21f, 12f)
        curveTo(21f, 17f, 17f, 21f, 12f, 21f)
        close()
        moveTo(12f, 3.5f)
        verticalLineTo(21f)
        moveTo(5f, 6.5f)
        lineTo(9.5f, 20f)
        moveTo(19f, 6.5f)
        lineTo(14.5f, 20f)
        moveTo(3f, 12f)
        lineTo(7f, 19f)
        moveTo(21f, 12f)
        lineTo(17f, 19f)
    }
}