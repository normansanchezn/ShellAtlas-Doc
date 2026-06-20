package com.shelldocs.core.designsystem.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shelldocs.core.designsystem.tokens.ShellTypography.Companion.MIN_FONT_SIZE

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
        /**
         * Desktop/web scale. Bumped per the responsive typography pass: page
         * titles 16->32sp, body/sidebar 13->16sp, labels/metadata 12->14sp,
         * code/editor 12->14sp. `caption` and `sectionLabel` are raised to the
         * 14sp floor (see [MIN_FONT_SIZE]) — nothing renders below it.
         */
        fun default(
            sans: FontFamily = FontFamily.SansSerif,
            mono: FontFamily = FontFamily.Monospace,
        ): ShellTypography = ShellTypography(
            displayTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
            pageTitle = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.2).sp
            ),
            sectionTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
            body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
            bodyStrong = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 23.sp
            ),
            label = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
            caption = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.Normal,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 18.sp
            ),
            sectionLabel = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp
            ),
            metricValue = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp),
            code = TextStyle(
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 20.sp
            ),
        )

        /** Mobile scale, already roomy; only the two sub-14sp tokens are raised to the floor. */
        fun mobile(
            sans: FontFamily = FontFamily.SansSerif,
            mono: FontFamily = FontFamily.Monospace,
        ): ShellTypography = ShellTypography(
            displayTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
            pageTitle = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.3).sp
            ),
            sectionTitle = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
            body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
            bodyStrong = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 26.sp
            ),
            label = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
            caption = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.Normal,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 18.sp
            ),
            sectionLabel = TextStyle(
                fontFamily = sans,
                fontWeight = FontWeight.SemiBold,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 18.sp,
                letterSpacing = 0.4.sp
            ),
            metricValue = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.35).sp),
            code = TextStyle(
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = MIN_FONT_SIZE,
                lineHeight = 20.sp
            ),
        )

        /** No user-facing text renders below this, even after [scaled] zooms out. */
        val MIN_FONT_SIZE = 14.sp

        private fun TextStyle.scale(factor: Float): TextStyle = copy(
            fontSize = if (fontSize.value * factor < MIN_FONT_SIZE.value) MIN_FONT_SIZE else fontSize * factor,
            lineHeight = lineHeight * factor,
        )
    }
}
