package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconCopy: ImageVector by lazy {
    shellIcon("IconCopy") {
        roundedRect(9f, 9f, 12f, 12f, 2f)
        moveTo(5f, 15f)
        lineTo(5f, 5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 7f, y1 = 3f)
        lineTo(15f, 3f)
    }
}
