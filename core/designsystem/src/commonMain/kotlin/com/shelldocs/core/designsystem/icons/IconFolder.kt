package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconFolder: ImageVector by lazy {
    shellIcon("IconFolder") {
        moveTo(22f, 19f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 20f, y1 = 21f)
        horizontalLineTo(4f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 2f, y1 = 19f)
        verticalLineTo(5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 4f, y1 = 3f)
        horizontalLineTo(9f)
        lineTo(11f, 6f)
        horizontalLineTo(20f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 22f, y1 = 8f)
        close()
    }
}
