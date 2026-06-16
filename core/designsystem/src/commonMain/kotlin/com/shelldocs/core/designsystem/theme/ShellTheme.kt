package com.shelldocs.core.designsystem.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.shelldocs.core.designsystem.tokens.ShellColorScheme
import com.shelldocs.core.designsystem.tokens.ShellDarkColors
import com.shelldocs.core.designsystem.tokens.ShellLightColors
import com.shelldocs.core.designsystem.tokens.ShellTypography

val LocalShellColors = staticCompositionLocalOf { ShellLightColors }
val LocalShellTypography = staticCompositionLocalOf { ShellTypography.default() }

/** Accessors used by every component: `ShellTheme.colors`, `ShellTheme.typography`. */
object ShellTheme {
    val colors: ShellColorScheme
        @Composable get() = LocalShellColors.current
    val typography: ShellTypography
        @Composable get() = LocalShellTypography.current
}

@Composable
fun ShellDocsTheme(
    darkTheme: Boolean = false,
    textScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colors: ShellColorScheme = if (darkTheme) ShellDarkColors else ShellLightColors
    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = colors.brand,
            onPrimary = colors.onBrand,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.border,
        )
    } else {
        lightColorScheme(
            primary = colors.brand,
            onPrimary = colors.onBrand,
            background = colors.background,
            surface = colors.surface,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            outline = colors.border,
        )
    }
    val selectionColors = TextSelectionColors(
        handleColor = colors.brand,
        backgroundColor = colors.brand.copy(alpha = 0.3f),
    )
    CompositionLocalProvider(
        LocalShellColors provides colors,
        LocalShellTypography provides ShellTypography.default().scaled(textScale),
        LocalTextSelectionColors provides selectionColors,
    ) {
        MaterialTheme(colorScheme = materialColors, content = content)
    }
}
