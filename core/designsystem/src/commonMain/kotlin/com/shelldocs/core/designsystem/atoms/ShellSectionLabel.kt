package com.shelldocs.core.designsystem.atoms

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.theme.ShellTheme

/** Uppercase 10sp tracking-0.5 label ("KNOWLEDGE", "ANALYTICS", "SOURCES"). */
@Composable
fun ShellSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = ShellTheme.typography.sectionLabel,
        color = ShellTheme.colors.textMuted,
        modifier = modifier,
    )
}
