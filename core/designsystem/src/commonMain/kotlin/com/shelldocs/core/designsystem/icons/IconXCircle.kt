package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconXCircle: ImageVector by lazy {
    shellIcon("IconXCircle") {
        circle(12f, 12f, 9f)
        moveTo(9f, 9f)
        lineTo(15f, 15f)
        moveTo(15f, 9f)
        lineTo(9f, 15f)
    }
}
