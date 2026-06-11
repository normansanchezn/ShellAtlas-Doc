package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconCheckCircle
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.molecules.ShellMetricCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.dashboard.presentation.DashboardIntent
import com.shelldocs.feature.dashboard.presentation.DashboardViewModel

/** Dashboard: stat cards, health ring, coverage, status donut, usage, activity. */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) { viewModel.onIntent(DashboardIntent.Initialize) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(ShellSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dashboard", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(
                    "Knowledge operations overview",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
            ShellGhostButton(
                text = "Refresh",
                icon = IconRefresh,
                onClick = { viewModel.onIntent(DashboardIntent.Refresh) },
            )
        }

        val metrics = state.metrics
        if (metrics == null) {
            Text(
                text = state.errorMessage ?: "Loading metrics...",
                style = ShellTheme.typography.body,
                color = colors.textMuted,
            )
            return@Column
        }

        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                MetricCards(metrics = metrics, modifier = Modifier.weight(1f))
            }
        } else {
            MetricCards(metrics = metrics, modifier = Modifier.fillMaxWidth(), stacked = true)
        }

        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg)) {
                KnowledgeHealthCard(metrics = metrics, modifier = Modifier.weight(1.1f))
                ModuleCoverageCard(metrics = metrics, modifier = Modifier.weight(1.2f))
                StatusDonutCard(metrics = metrics, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg)) {
                UsageChartCard(metrics = metrics, modifier = Modifier.weight(1.6f))
                RecentActivityCard(metrics = metrics, modifier = Modifier.weight(1f))
            }
        } else {
            KnowledgeHealthCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
            ModuleCoverageCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
            StatusDonutCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
            UsageChartCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
            RecentActivityCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
        }
        NeedsAttentionRow(metrics = metrics, isWide = isWide)
    }
}

@Composable
private fun MetricCards(
    metrics: com.shelldocs.core.domain.entity.dashboard.DashboardMetrics,
    modifier: Modifier = Modifier,
    stacked: Boolean = false,
) {
    val colors = ShellTheme.colors
    val cards: List<@Composable (Modifier) -> Unit> = listOf(
        { m ->
            ShellMetricCard(
                icon = IconFileText, iconTint = colors.info,
                value = "${metrics.totalDocuments}", caption = "Total Documents · this week",
                delta = "↗ +${metrics.totalDocumentsDelta}", modifier = m,
            )
        },
        { m ->
            ShellMetricCard(
                icon = IconAlertTriangle, iconTint = colors.warning,
                value = "${metrics.outdatedDocuments}", caption = "Outdated Docs · this week",
                delta = "↗ +${metrics.outdatedDocumentsDelta}", deltaColor = colors.danger, modifier = m,
            )
        },
        { m ->
            ShellMetricCard(
                icon = IconCheckCircle, iconTint = colors.accentTeal,
                value = "${metrics.coverageScorePercent}%", caption = "Coverage Score", modifier = m,
            )
        },
        { m ->
            ShellMetricCard(
                icon = IconMessageSquare, iconTint = colors.brand,
                value = "${metrics.aiQueriesThisWeek}", caption = "AI Queries · vs last wk",
                delta = "↗ +${metrics.aiQueriesDeltaPercent}%", modifier = m,
            )
        },
    )
    if (stacked) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
            cards.forEach { card -> card(Modifier.fillMaxWidth()) }
        }
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
            cards.forEach { card ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { card(Modifier.fillMaxWidth()) }
            }
        }
    }
}
