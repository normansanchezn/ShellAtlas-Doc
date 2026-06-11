package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconMoon: ImageVector by lazy {
    shellIcon("IconMoon") {
        moveTo(21f, 12.8f)
        arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 11.2f, y1 = 3f)
        arcTo(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 21f, y1 = 12.8f)
        close()
    }
}
