package com.shelldocs.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconGitBranch: ImageVector by lazy {
    shellIcon("IconGitBranch") {
        moveTo(6f, 3f)
        verticalLineTo(15f)
        circle(6f, 18f, 3f)
        circle(18f, 6f, 3f)
        moveTo(18f, 9f)
        arcTo(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 9f, y1 = 18f)
    }
}
