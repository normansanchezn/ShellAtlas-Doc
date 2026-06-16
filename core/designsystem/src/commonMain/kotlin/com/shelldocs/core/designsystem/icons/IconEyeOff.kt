package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconEyeOff: ImageVector = shellIcon("EyeOff") {
    // partial eye shape (top)
    moveTo(17.94f, 17.94f)
    cubicTo(16.23f, 19.24f, 14.16f, 20f, 12f, 20f)
    cubicTo(5f, 20f, 1f, 12f, 1f, 12f)
    cubicTo(1f, 12f, 3.27f, 8.09f, 6.06f, 6.06f)
    // partial eye shape (bottom)
    moveTo(9.9f, 4.24f)
    cubicTo(10.59f, 4.08f, 11.29f, 4f, 12f, 4f)
    cubicTo(19f, 4f, 23f, 12f, 23f, 12f)
    cubicTo(23f, 12f, 21.82f, 13.98f, 20.16f, 15.76f)
    // iris partial
    moveTo(14.12f, 14.12f)
    cubicTo(13.56f, 14.64f, 12.81f, 14.97f, 12f, 14.97f)
    cubicTo(10.35f, 14.97f, 9.03f, 13.65f, 9.03f, 12f)
    cubicTo(9.03f, 11.19f, 9.36f, 10.44f, 9.88f, 9.88f)
    // diagonal slash
    moveTo(1f, 1f)
    lineTo(23f, 23f)
}
