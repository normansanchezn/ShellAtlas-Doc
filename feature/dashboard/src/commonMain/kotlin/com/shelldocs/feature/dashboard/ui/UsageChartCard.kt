package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.BarEntry
import com.shelldocs.core.designsystem.molecules.ShellBarChart
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** AI Assistant Usage bar chart; peak day rendered in brand yellow. */
@Composable
fun UsageChartCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val peak = metrics.usage.maxOfOrNull { it.queries } ?: 0
    ShellCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "AI Assistant Usage",
                    style = ShellTheme.typography.sectionTitle,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "✦ ${metrics.aiQueriesThisWeek} queries this week",
                    style = ShellTheme.typography.caption,
                    color = colors.brand,
                )
            }
            ShellBarChart(
                entries = metrics.usage.map { point ->
                    BarEntry(
                        label = point.dayLabel,
                        value = point.queries,
                        highlighted = point.queries == peak,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(124.dp).padding(top = ShellSpacing.lg),
            )
        }
    }
}
