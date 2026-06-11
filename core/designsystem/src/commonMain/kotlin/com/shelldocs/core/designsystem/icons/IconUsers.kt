package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconUsers: ImageVector by lazy {
    shellIcon("IconUsers") {
        circle(9f, 7f, 4f)
        moveTo(2f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 6f, y1 = 15f)
        horizontalLineTo(12f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 16f, y1 = 19f)
        verticalLineTo(21f)
        moveTo(16f, 3.1f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 16f, y1 = 10.9f)
        moveTo(22f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 19f, y1 = 15.1f)
    }
}
