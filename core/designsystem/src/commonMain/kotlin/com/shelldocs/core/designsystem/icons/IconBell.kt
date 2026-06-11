package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconBell: ImageVector by lazy {
    shellIcon("IconBell") {
        moveTo(18f, 8f)
        arcTo(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 6f, y1 = 8f)
        curveTo(6f, 15f, 3f, 17f, 3f, 17f)
        horizontalLineTo(21f)
        curveTo(21f, 17f, 18f, 15f, 18f, 8f)
        close()
        moveTo(10.3f, 21f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 13.7f, y1 = 21f)
    }
}
