package com.shelldocs.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing

/** Integrations: pointer to the Sources screen where syncs are managed. */
@Composable
fun IntegrationsSection(modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.widthIn(max = 520.dp)) {
        Text("Integrations", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        ShellCard(modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.md)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs),
            ) {
                Text(
                    text = "Confluence · Azure DevOps · Jira",
                    style = ShellTheme.typography.bodyStrong,
                    color = colors.textPrimary,
                )
                Text(
                    text = "Connection status, sync runs and reconnection live in the Sources section of the sidebar.",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
        }
    }
}
