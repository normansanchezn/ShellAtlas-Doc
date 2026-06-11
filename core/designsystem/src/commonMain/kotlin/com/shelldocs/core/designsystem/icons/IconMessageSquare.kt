package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconMessageSquare: ImageVector by lazy {
    shellIcon("IconMessageSquare") {
        moveTo(21f, 15f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 19f, y1 = 17f)
        horizontalLineTo(7f)
        lineTo(3f, 21f)
        verticalLineTo(5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 5f, y1 = 3f)
        horizontalLineTo(19f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 21f, y1 = 5f)
        close()
    }
}
