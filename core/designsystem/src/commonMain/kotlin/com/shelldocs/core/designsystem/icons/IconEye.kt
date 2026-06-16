package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconEye: ImageVector = shellIcon("Eye") {
    // outer eye shape
    moveTo(1f, 12f)
    cubicTo(1f, 12f, 5f, 5f, 12f, 5f)
    cubicTo(19f, 5f, 23f, 12f, 23f, 12f)
    cubicTo(23f, 12f, 19f, 19f, 12f, 19f)
    cubicTo(5f, 19f, 1f, 12f, 1f, 12f)
    close()
    // iris circle
    circle(12f, 12f, 3f)
}
