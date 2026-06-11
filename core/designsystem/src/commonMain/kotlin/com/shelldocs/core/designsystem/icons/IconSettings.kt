package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconSettings: ImageVector by lazy {
    shellIcon("IconSettings") {
        circle(12f, 12f, 3f)
        // gear teeth as radial ticks
        moveTo(12f, 2.5f); verticalLineTo(5.5f)
        moveTo(12f, 18.5f); verticalLineTo(21.5f)
        moveTo(2.5f, 12f); horizontalLineTo(5.5f)
        moveTo(18.5f, 12f); horizontalLineTo(21.5f)
        moveTo(5.3f, 5.3f); lineTo(7.4f, 7.4f)
        moveTo(16.6f, 16.6f); lineTo(18.7f, 18.7f)
        moveTo(18.7f, 5.3f); lineTo(16.6f, 7.4f)
        moveTo(7.4f, 16.6f); lineTo(5.3f, 18.7f)
    }
}
