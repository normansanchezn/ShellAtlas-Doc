package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.DonutSlice
import com.shelldocs.core.designsystem.molecules.ShellDonutChart
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.core.domain.entity.document.DocumentStatus

/** Status de la documentación actual: donut + legend, built from whatever statuses exist right now. */
@Composable
fun StatusBreakdownCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val legend = metrics.statusBreakdown.map { it.status.displayName to it.percent to statusColor(it.status, colors) }
    ShellCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl)) {
            Text("Status de la Documentación", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = ShellSpacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                ShellDonutChart(
                    slices = legend.map { (labelPercent, color) -> DonutSlice(labelPercent.second / 100f, color) },
                    modifier = Modifier.size(110.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                legend.forEach { (labelPercent, color) -> LegendRow(labelPercent.first, labelPercent.second, color) }
            }
        }
    }
}

private fun statusColor(
    status: DocumentStatus,
    colors: com.shelldocs.core.designsystem.tokens.ShellColorScheme
): Color = when (status) {
    DocumentStatus.PUBLISHED -> colors.accentTeal
    DocumentStatus.OUTDATED -> colors.warning
    DocumentStatus.DRAFT -> colors.textMuted
    DocumentStatus.UPDATES_PENDING -> colors.brand
    DocumentStatus.CONFLICTED -> colors.danger
    DocumentStatus.ARCHIVED -> colors.textMuted
    DocumentStatus.LOCKED -> colors.info
    DocumentStatus.DELETED_SOURCE -> colors.danger
}

@Composable
private fun LegendRow(label: String, percent: Int, color: Color) {
    val colors = ShellTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(
            text = label,
            style = ShellTheme.typography.caption,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f).padding(start = ShellSpacing.sm),
        )
        Text(text = "$percent%", style = ShellTheme.typography.caption, color = colors.textPrimary)
    }
}
