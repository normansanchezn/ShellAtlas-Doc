package com.shelldocs.feature.sources.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.source.SyncLogEntry
import com.shelldocs.core.domain.entity.source.SyncOutcome

/** Sync Activity Log: outcome dot, timestamp, message, +new/updated counters. */
@Composable
fun SyncLogPanel(entries: List<SyncLogEntry>, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sync Activity Log",
                    style = ShellTheme.typography.sectionTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(text = "View full log", style = ShellTheme.typography.caption, color = colors.textMuted)
            }
            entries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (entry.outcome == SyncOutcome.SUCCESS) colors.success else colors.danger,
                            ),
                    )
                    Text(
                        text = entry.occurredAt.toString().substringAfter('T').take(5),
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                        modifier = Modifier.width(72.dp).padding(start = ShellSpacing.sm),
                    )
                    Text(
                        text = entry.message,
                        style = ShellTheme.typography.label,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (entry.outcome == SyncOutcome.SUCCESS) {
                            "+${entry.newDocs} new, ${entry.updatedDocs} updated"
                        } else {
                            "—"
                        },
                        style = ShellTheme.typography.caption,
                        color = if (entry.outcome == SyncOutcome.SUCCESS) colors.success else colors.textMuted,
                    )
                }
            }
        }
    }
}
