package com.shelldocs.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color slots of the ShellDocs design system. Values come from the
 * "Enterprise Knowledge Management Redesign" Figma file, pixel for pixel.
 */
@Immutable
data class ShellColorScheme(
    val brand: Color,
    val onBrand: Color,
    val background: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceSelected: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val accentTeal: Color,
    val isDark: Boolean,
)
