package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconBookmark: ImageVector by lazy {
    shellIcon("IconBookmark") {
        moveTo(19f, 21f)
        lineTo(12f, 16f)
        lineTo(5f, 21f)
        verticalLineTo(5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 7f, y1 = 3f)
        horizontalLineTo(17f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 19f, y1 = 5f)
        close()
    }
}
