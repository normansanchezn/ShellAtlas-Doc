package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.ShellHealthRing
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellRadius
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Knowledge Health ring plus the four sub-stats grid. */
@Composable
fun KnowledgeHealthCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShellHealthRing(score = metrics.knowledgeHealthScore, modifier = Modifier.size(120.dp))
            Text(
                text = "Knowledge Health",
                style = ShellTheme.typography.sectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = ShellSpacing.md),
            )
            Text(
                text = if (metrics.knowledgeHealthScore >= 70) "Good — keep it up" else "Good — needs attention",
                style = ShellTheme.typography.caption,
                color = colors.textMuted,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
            ) {
                HealthSubStat("${metrics.docsReviewedPercent}%", "Docs reviewed", Modifier.weight(1f))
                HealthSubStat("${metrics.sourcesSynced}/${metrics.sourcesTotal}", "Sources synced", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = ShellSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(ShellSpacing.sm),
            ) {
                HealthSubStat("${metrics.aiAccuracyPercent}%", "AI accuracy", Modifier.weight(1f))
                HealthSubStat("${metrics.staleRatePercent}%", "Stale rate", Modifier.weight(1f), warn = true)
            }
        }
    }
}

@Composable
private fun HealthSubStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    warn: Boolean = false,
) {
    val colors = ShellTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ShellRadius.sm))
            .background(colors.surfaceSubtle)
            .padding(vertical = ShellSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = ShellTheme.typography.bodyStrong,
            color = if (warn) colors.warning else colors.success,
        )
        Text(text = label, style = ShellTheme.typography.caption, color = colors.textMuted)
    }
}
