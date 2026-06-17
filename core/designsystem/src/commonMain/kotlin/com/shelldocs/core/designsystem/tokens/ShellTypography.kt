package com.shelldocs.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Shared type scale.
 *
 * `default()` keeps the denser desktop/web rhythm.
 * `mobile()` raises the baseline to match native mobile readability and
 * accessibility expectations.
 */
@Immutable
data class ShellTypography(
    val displayTitle: TextStyle,
    val pageTitle: TextStyle,
    val sectionTitle: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val sectionLabel: TextStyle,
    val metricValue: TextStyle,
    val code: TextStyle,
) {
    fun scaled(factor: Float): ShellTypography = if (factor == 1f) this else ShellTypography(
        displayTitle = displayTitle.scale(factor),
        pageTitle = pageTitle.scale(factor),
        sectionTitle = sectionTitle.scale(factor),
        body = body.scale(factor),
        bodyStrong = bodyStrong.scale(factor),
        label = label.scale(factor),
        caption = caption.scale(factor),
        sectionLabel = sectionLabel.scale(factor),
        metricValue = metricValue.scale(factor),
        code = code.scale(factor),
    )

    companion object {
        fun default(
            sans: FontFamily = FontFamily.SansSerif,
            mono: FontFamily = FontFamily.Monospace,
        ): ShellTypography = ShellTypography(
            displayTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
            pageTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.2).sp),
            sectionTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
            body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.5.sp),
            bodyStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 19.5.sp),
            label = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
            caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 13.sp),
            sectionLabel = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp),
            metricValue = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp),
            code = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
        )

        fun mobile(
            sans: FontFamily = FontFamily.SansSerif,
            mono: FontFamily = FontFamily.Monospace,
        ): ShellTypography = ShellTypography(
            displayTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
            pageTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
            sectionTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
            body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
            bodyStrong = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
            label = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
            caption = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
            sectionLabel = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
            metricValue = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.35).sp),
            code = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        )

        private fun TextStyle.scale(factor: Float): TextStyle = copy(
            fontSize = fontSize * factor,
            lineHeight = lineHeight * factor,
        )
    }
}
