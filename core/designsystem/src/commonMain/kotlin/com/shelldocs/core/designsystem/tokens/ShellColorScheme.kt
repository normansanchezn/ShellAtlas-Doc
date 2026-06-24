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
    /**
     * Brand-colored text/icon on a flat [surface]/[background] (selected nav state, chip text,
     * sparkles glyph). [brand] itself (`#FFD100`) fails contrast on white — only safe as a solid
     * fill paired with [onBrand], never as foreground content. Use this instead.
     */
    val accentText: Color,
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
