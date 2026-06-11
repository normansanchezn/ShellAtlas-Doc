package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

/** Document Status donut with legend. */
@Composable
fun StatusDonutCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val breakdown = metrics.statusBreakdown
    val legend = listOf(
        Triple("Published", breakdown.publishedPercent, colors.accentTeal),
        Triple("Outdated", breakdown.outdatedPercent, colors.warning),
        Triple("Draft", breakdown.draftPercent, colors.textMuted),
        Triple("Pending", breakdown.pendingPercent, colors.brand),
    )
    ShellCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl)) {
            Text("Document Status", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = ShellSpacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                ShellDonutChart(
                    slices = legend.map { (_, percent, color) -> DonutSlice(percent / 100f, color) },
                    modifier = Modifier.size(110.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                legend.forEach { (label, percent, color) -> LegendRow(label, percent, color) }
            }
        }
    }
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
