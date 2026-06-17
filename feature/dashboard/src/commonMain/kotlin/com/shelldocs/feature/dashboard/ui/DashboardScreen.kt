package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.testTag
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconCheckCircle
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.icons.IconSparkles
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
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

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = if (state.isLoading) "Refreshing..." else "Refresh",
                    icon = IconRefresh,
                    onClick = { viewModel.onIntent(DashboardIntent.Refresh) },
                    enabled = !state.isLoading,
                    modifier = Modifier.testTag(DemoTestTags.DashboardRefresh),
                )
            }

            val metrics = state.metrics
            if (metrics != null) {
                if (isWide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                        MetricCards(metrics = metrics, modifier = Modifier.weight(1f))
                    }
                } else {
                    MetricCards(
                        metrics = metrics,
                        modifier = Modifier.fillMaxWidth(),
                        columns = 3,
                    )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
                    ) {
                        KnowledgeHealthCard(metrics = metrics, modifier = Modifier.weight(1f))
                        StatusDonutCard(metrics = metrics, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md),
                    ) {
                        ModuleCoverageCard(metrics = metrics, modifier = Modifier.weight(1f))
                        UsageChartCard(metrics = metrics, modifier = Modifier.weight(1f))
                    }
                    RecentActivityCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
                }
                NeedsAttentionRow(metrics = metrics, isWide = isWide)
            }
        }

        if (state.isLoading) {
            ShellLoadingOverlay(message = "Loading dashboard...")
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(DashboardIntent.DismissError) },
        )
    }
}

@Composable
private fun MetricCards(
    metrics: com.shelldocs.core.domain.entity.dashboard.DashboardMetrics,
    modifier: Modifier = Modifier,
    columns: Int = 4,
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
        { m ->
            ShellMetricCard(
                icon = IconSparkles, iconTint = colors.success,
                value = "${metrics.projectKnowledgeScorePercent}%", caption = "Project Knowledge",
                delta = "${metrics.knowledgeCheckpointsCompleted}/${metrics.knowledgeCheckpointsTotal} checkpoints",
                deltaColor = colors.textMuted, modifier = m,
            )
        },
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
        cards.chunked(columns).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                row.forEach { card ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        card(Modifier.fillMaxWidth())
                    }
                }
                repeat((columns - row.size).coerceAtLeast(0)) {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
