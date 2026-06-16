package com.shelldocs.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme

/**
 * Full-bleed static background for authenticated workspace screens.
 *
 * Extremely faint dot grid on the theme background color — visible enough to
 * add texture, invisible enough to never compete with content.
 * No animation; keeps the workspace calm and readable.
 */
@Composable
fun ShellWorkspaceBackground(modifier: Modifier = Modifier) {
    val dotColor = ShellTheme.colors.textMuted.copy(alpha = 0.06f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val spacing = 28.dp.toPx()
        val radius = 1.2f
        var x = spacing / 2f
        while (x < size.width) {
            var y = spacing / 2f
            while (y < size.height) {
                drawCircle(color = dotColor, radius = radius, center = Offset(x, y))
                y += spacing
            }
            x += spacing
        }
    }
}
