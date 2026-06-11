package com.shelldocs.core.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius

/** Keyboard shortcut chip (e.g. "⌘ K") in JetBrains-Mono style. */
@Composable
fun ShellKbdHint(text: String, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Text(
        text = text,
        style = ShellTheme.typography.code,
        color = colors.textMuted,
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.surfaceSubtle)
            .border(1.dp, colors.borderStrong, RoundedCornerShape(ShellRadius.sm))
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}
