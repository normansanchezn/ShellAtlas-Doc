package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconLanguage: ImageVector by lazy {
    shellIcon("IconLanguage") {
        // outer circle
        circle(12f, 12f, 10f)
        // horizontal equator line
        moveTo(2f, 12f); horizontalLineTo(22f)
        // left meridian arc (bulges left)
        moveTo(12f, 2f)
        arcTo(5f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 12f, y1 = 22f)
        // right meridian arc (bulges right)
        moveTo(12f, 2f)
        arcTo(5f, 10f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 12f, y1 = 22f)
    }
}
