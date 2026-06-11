package com.shelldocs.feature.sources.ui

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
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconClock
import com.shelldocs.core.designsystem.icons.IconDatabase
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconPlus
import com.shelldocs.core.designsystem.molecules.ShellMetricCard
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.sources.presentation.SourcesIntent
import com.shelldocs.feature.sources.presentation.SourcesViewModel

/** Imported Sources: stats, integration rows, sync activity log. */
@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) { viewModel.onIntent(SourcesIntent.Initialize) }

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
                Text("Imported Sources", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(
                    "Manage external knowledge integrations",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
            ShellPrimaryButton(text = "Add integration", icon = IconPlus, onClick = {})
        }

        val statCards: List<@Composable (Modifier) -> Unit> = listOf(
            { m ->
                ShellMetricCard(
                    icon = IconFileText, iconTint = colors.accentTeal,
                    value = "${state.totalImportedDocs}", caption = "Total imported docs", modifier = m,
                )
            },
            { m ->
                ShellMetricCard(
                    icon = IconDatabase, iconTint = colors.info,
                    value = "${state.activeIntegrations} / ${state.sources.size}",
                    caption = "Active integrations", modifier = m,
                )
            },
            { m ->
                ShellMetricCard(
                    icon = IconClock, iconTint = colors.brand,
                    value = state.lastSync?.toString()?.substringAfter('T')?.take(5) ?: "—",
                    caption = "Last sync", modifier = m,
                )
            },
        )
        if (isWide) {
            Row(horizontalArrangement = Arrangement.spacedBy(ShellSpacing.md)) {
                statCards.forEach { card ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        card(Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            statCards.forEach { card -> card(Modifier.fillMaxWidth()) }
        }

        Text("Integrations", style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
        state.sources.forEach { source ->
            IntegrationRow(
                source = source,
                isSyncing = source.id in state.syncingSourceIds,
                onSync = { viewModel.onIntent(SourcesIntent.Sync(source.id)) },
                onReconnect = { viewModel.onIntent(SourcesIntent.Reconnect(source.id)) },
            )
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                style = ShellTheme.typography.caption,
                color = colors.danger,
            )
        }
        SyncLogPanel(entries = state.syncLog)
    }
}
