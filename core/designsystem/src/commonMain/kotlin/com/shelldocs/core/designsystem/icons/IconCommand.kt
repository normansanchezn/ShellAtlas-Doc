package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconCommand: ImageVector by lazy {
    shellIcon("IconCommand") {
        moveTo(15f, 6f)
        verticalLineTo(18f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 18f, y1 = 15f)
        horizontalLineTo(6f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 9f, y1 = 18f)
        verticalLineTo(6f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 6f, y1 = 9f)
        horizontalLineTo(18f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, x1 = 15f, y1 = 6f)
        close()
    }
}
