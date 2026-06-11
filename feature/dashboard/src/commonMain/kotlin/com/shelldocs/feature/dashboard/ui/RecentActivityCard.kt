package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellAvatar
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.ActivityKind
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import kotlin.time.ExperimentalTime

/** Recent Activity feed (actor + verb + target). */
@OptIn(ExperimentalTime::class)
@Composable
fun RecentActivityCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Recent Activity",
                    style = ShellTheme.typography.sectionTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(text = "View all", style = ShellTheme.typography.caption, color = colors.textMuted)
            }
            metrics.recentActivity.forEach { event ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShellAvatar(
                        initials = event.actorInitials,
                        size = 22.dp,
                        color = colors.surfaceSubtle,
                        contentColor = colors.textSecondary,
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm)) {
                        Text(
                            text = "${event.actorName} ${event.kind.verb()}",
                            style = ShellTheme.typography.label,
                            color = colors.textPrimary,
                            maxLines = 1,
                        )
                        Text(
                            text = event.target,
                            style = ShellTheme.typography.caption,
                            color = colors.textMuted,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = event.occurredAt.toString().substringAfter('T').take(5),
                        style = ShellTheme.typography.caption,
                        color = colors.textMuted,
                    )
                }
            }
        }
    }
}

private fun ActivityKind.verb(): String = when (this) {
    ActivityKind.PUBLISHED -> "published"
    ActivityKind.EDITED -> "edited"
    ActivityKind.REVIEWED -> "reviewed"
    ActivityKind.CREATED -> "created"
    ActivityKind.MARKED_OUTDATED -> "marked outdated"
    ActivityKind.FLAGGED_STALE -> "flagged stale"
}
