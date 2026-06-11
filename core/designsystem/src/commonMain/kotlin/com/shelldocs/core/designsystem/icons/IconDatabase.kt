package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconDatabase: ImageVector by lazy {
    shellIcon("IconDatabase") {
        moveTo(3f, 5f)
        arcTo(9f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 21f, y1 = 5f)
        arcTo(9f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 3f, y1 = 5f)
        moveTo(3f, 5f)
        verticalLineTo(19f)
        arcTo(9f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 21f, y1 = 19f)
        verticalLineTo(5f)
        moveTo(3f, 12f)
        arcTo(9f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 21f, y1 = 12f)
    }
}
