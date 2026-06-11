package com.shelldocs.core.designsystem.molecules

/** One bar of a [ShellBarChart]. */
data class BarEntry(
    val label: String,
    val value: Int,
    val highlighted: Boolean = false,
)
