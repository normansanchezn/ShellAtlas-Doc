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
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.molecules.ShellProgressBar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics

/** Module Coverage: one labelled meter per app module. */
@Composable
fun ModuleCoverageCard(metrics: DashboardMetrics, modifier: Modifier = Modifier) {
    val colors = ShellTheme.colors
    ShellCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(ShellSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.md),
        ) {
            Text("Module Coverage", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            metrics.moduleCoverage.forEach { coverage ->
                Column(verticalArrangement = Arrangement.spacedBy(ShellSpacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = coverage.module,
                            style = ShellTheme.typography.label,
                            color = colors.textSecondary,
                        )
                        Text(
                            text = "${coverage.coveragePercent}%",
                            style = ShellTheme.typography.label,
                            color = when {
                                coverage.coveragePercent >= 80 -> colors.accentTeal
                                coverage.coveragePercent >= 60 -> colors.brand
                                else -> colors.warning
                            },
                        )
                    }
                    ShellProgressBar(
                        progress = coverage.coveragePercent / 100f,
                        color = when {
                            coverage.coveragePercent >= 80 -> colors.accentTeal
                            coverage.coveragePercent >= 60 -> colors.brand
                            else -> colors.warning
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
