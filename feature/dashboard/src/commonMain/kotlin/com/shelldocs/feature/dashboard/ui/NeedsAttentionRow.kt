package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconTrendingUp
import com.shelldocs.core.designsystem.icons.IconXCircle
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.AttentionItem
import com.shelldocs.core.domain.entity.dashboard.AttentionSeverity
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** "Needs Attention" banners at the bottom of the dashboard. */
@Composable
fun NeedsAttentionRow(
    metrics: DashboardMetrics,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ShellSpacing.sm)) {
        Text("Needs Attention", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                metrics.attentionItems.forEach { item ->
                    AttentionCard(item = item, modifier = Modifier.weight(1f))
                }
            }
        } else {
            metrics.attentionItems.forEach { item ->
                AttentionCard(item = item, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun AttentionCard(item: AttentionItem, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    val (icon, tint) = when (item.severity) {
        AttentionSeverity.ERROR -> IconXCircle to colors.danger
        AttentionSeverity.WARNING -> IconAlertTriangle to colors.warning
        AttentionSeverity.INFO -> IconTrendingUp to colors.info
    }
    ShellCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.headline, style = ShellTheme.typography.bodyStrong, color = colors.textPrimary)
                Text(item.detail, style = ShellTheme.typography.caption, color = colors.textMuted)
            }
        }
    }
}
