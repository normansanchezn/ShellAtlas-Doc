package com.shelldocs.feature.updates.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.updates.RiskLevel
import com.shelldocs.feature.updates.presentation.UpdatesIntent
import com.shelldocs.feature.updates.presentation.UpdatesState

/** Four tappable cards: Critical / High / Medium / Low counters (filters). */
@Composable
fun RiskSummaryRow(
    state: UpdatesState,
    onIntent: (UpdatesIntent) -> Unit,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = ShellTheme.colors
    val riskColor = mapOf(
        RiskLevel.CRITICAL to colors.danger,
        RiskLevel.HIGH to colors.warning,
        RiskLevel.MEDIUM to colors.brand,
        RiskLevel.LOW to colors.info,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
    ) {
        RiskLevel.entries.forEach { risk ->
            ShellCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onIntent(UpdatesIntent.ToggleRiskFilter(risk)) },
            ) {
                Column(modifier = Modifier.padding(ShellSpacing.lg)) {
                    Text(
                        text = "${state.countsByRisk[risk] ?: 0}",
                        style = ShellTheme.typography.metricValue,
                        color = if (state.riskFilter == null || state.riskFilter == risk) {
                            riskColor.getValue(risk)
                        } else {
                            colors.textMuted
                        },
                    )
                    Text(
                        text = "${risk.displayName} Risk",
                        style = ShellTheme.typography.caption,
                        color = riskColor.getValue(risk),
                    )
                }
            }
        }
    }
}
