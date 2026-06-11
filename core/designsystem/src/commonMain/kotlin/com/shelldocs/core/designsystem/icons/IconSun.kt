package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconSun: ImageVector by lazy {
    shellIcon("IconSun") {
        circle(12f, 12f, 4f)
        moveTo(12f, 2f); verticalLineTo(4f)
        moveTo(12f, 20f); verticalLineTo(22f)
        moveTo(2f, 12f); horizontalLineTo(4f)
        moveTo(20f, 12f); horizontalLineTo(22f)
        moveTo(4.9f, 4.9f); lineTo(6.3f, 6.3f)
        moveTo(17.7f, 17.7f); lineTo(19.1f, 19.1f)
        moveTo(19.1f, 4.9f); lineTo(17.7f, 6.3f)
        moveTo(6.3f, 17.7f); lineTo(4.9f, 19.1f)
    }
}
