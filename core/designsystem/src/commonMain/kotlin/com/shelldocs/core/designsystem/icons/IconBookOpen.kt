package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconBookOpen: ImageVector by lazy {
    shellIcon("IconBookOpen") {
        moveTo(2f, 3f)
        horizontalLineTo(8f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 12f, y1 = 7f)
        verticalLineTo(21f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 9f, y1 = 18f)
        horizontalLineTo(2f)
        close()
        moveTo(22f, 3f)
        horizontalLineTo(16f)
        arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 12f, y1 = 7f)
        verticalLineTo(21f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 15f, y1 = 18f)
        horizontalLineTo(22f)
        close()
    }
}
