package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.ShellProgressBar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Coverage por área: one labelled meter per [com.shelldocs.core.domain.entity.document.Area]. */
@Composable
fun AreaCoverageCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Text("Coverage por Área", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            metrics.areaCoverage.forEach { coverage ->
                Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${coverage.area} (${coverage.documentCount})",
                            style = ShellTheme.typography.label,
                            color = colors.textSecondary,
                        )
                        Text(
                            text = "${coverage.healthyPercent}%",
                            style = ShellTheme.typography.label,
                            color = when {
                                coverage.healthyPercent >= 80 -> colors.accentTeal
                                coverage.healthyPercent >= 60 -> colors.brand
                                else -> colors.warning
                            },
                        )
                    }
                    ShellProgressBar(
                        progress = coverage.healthyPercent / 100f,
                        color = when {
                            coverage.healthyPercent >= 80 -> colors.accentTeal
                            coverage.healthyPercent >= 60 -> colors.brand
                            else -> colors.warning
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
