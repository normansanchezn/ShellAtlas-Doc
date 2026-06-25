package com.shelldocs.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconAlertTriangle
import com.shelldocs.core.designsystem.icons.IconCheckCircle
import com.shelldocs.core.designsystem.icons.IconMessageSquare
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.molecules.ShellMetricCard
import com.shelldocs.core.designsystem.molecules.ShellScreenToolbar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.dashboard.DashboardMetrics
import com.shelldocs.feature.dashboard.DashboardStringRes
import com.shelldocs.feature.dashboard.presentation.DashboardIntent
import com.shelldocs.feature.dashboard.presentation.DashboardViewModel

/** Dashboard: KT completion, healthy/attention counts, area coverage, status, AI usage, top owners. */
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
        Column(modifier = Modifier.fillMaxSize()) {
            ShellScreenToolbar(
                title = DashboardStringRes.TITLE,
                subtitle = DashboardStringRes.SUBTITLE,
                trailingContent = {
                    ShellGhostButton(
                        text = if (state.isLoading) DashboardStringRes.REFRESHING else DashboardStringRes.REFRESH,
                        icon = IconRefresh,
                        onClick = { viewModel.onIntent(DashboardIntent.Refresh) },
                        enabled = !state.isLoading,
                        modifier = Modifier.testTag(DemoTestTags.DashboardRefresh),
                    )
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
                verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
            ) {
                val metrics = state.metrics
                if (metrics != null) {
                    MetricCards(metrics = metrics, modifier = Modifier.fillMaxWidth(), columns = if (isWide) 3 else 1)

                    if (isWide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.lg)) {
                            KnowledgeTransferCard(metrics = metrics, modifier = Modifier.weight(1f))
                            AreaCoverageCard(metrics = metrics, modifier = Modifier.weight(1.2f))
                            StatusBreakdownCard(metrics = metrics, modifier = Modifier.weight(1f))
                        }
                    } else {
                        KnowledgeTransferCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
                        AreaCoverageCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
                        StatusBreakdownCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
                    }
                    TopOwnersCard(metrics = metrics, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        if (state.isLoading) {
            ShellLoadingOverlay(message = DashboardStringRes.LOADING)
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
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier,
    columns: Int = 3,
) {
    val colors = ShellTheme.colors
    val cards: List<@Composable (Modifier) -> Unit> = listOf(
        { m ->
            ShellMetricCard(
                icon = IconCheckCircle, iconTint = colors.success,
                value = "${metrics.healthyDocuments}", caption = "Healthy Documents", modifier = m,
            )
        },
        { m ->
            ShellMetricCard(
                icon = IconAlertTriangle, iconTint = colors.warning,
                value = "${metrics.attentionDocuments}", caption = "Needs Attention",
                deltaColor = colors.danger, modifier = m,
            )
        },
        { m ->
            ShellMetricCard(
                icon = IconMessageSquare, iconTint = colors.brand,
                value = "${metrics.aiUsageCount}", caption = "AI Assistant Usage", modifier = m,
            )
        },
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
        cards.chunked(columns).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                row.forEach { card ->
                    Box(modifier = Modifier.weight(1f)) {
                        card(Modifier.fillMaxWidth())
                    }
                }
                repeat((columns - row.size).coerceAtLeast(0)) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
