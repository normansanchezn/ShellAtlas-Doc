package com.shelldocs.core.designsystem.molecules

import androidx.compose.ui.graphics.Color

/** One slice of a [ShellDonutChart]. */
data class DonutSlice(
    val fraction: Float,
    val color: Color,
)
