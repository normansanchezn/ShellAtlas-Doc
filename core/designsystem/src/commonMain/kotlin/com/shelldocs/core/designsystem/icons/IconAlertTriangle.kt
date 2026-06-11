package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconAlertTriangle: ImageVector by lazy {
    shellIcon("IconAlertTriangle") {
        moveTo(10.3f, 3.9f)
        lineTo(1.8f, 18f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 3.5f, y1 = 21f)
        horizontalLineTo(20.5f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 22.2f, y1 = 18f)
        lineTo(13.7f, 3.9f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 10.3f, y1 = 3.9f)
        close()
        moveTo(12f, 9f)
        verticalLineTo(13f)
        moveTo(12f, 17f)
        lineTo(12.01f, 17f)
    }
}
