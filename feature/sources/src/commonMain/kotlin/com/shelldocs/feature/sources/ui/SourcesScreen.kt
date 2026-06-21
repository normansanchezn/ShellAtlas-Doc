package com.shelldocs.feature.sources.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconClock
import com.shelldocs.core.designsystem.icons.IconDatabase
import com.shelldocs.core.designsystem.icons.IconFileText
import com.shelldocs.core.designsystem.icons.IconPlus
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.molecules.ShellMetricCard
import com.shelldocs.core.designsystem.molecules.ShellScreenToolbar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.sources.SourcesStringRes
import com.shelldocs.feature.sources.presentation.SourcesIntent
import com.shelldocs.feature.sources.presentation.SourcesViewModel
import kotlin.time.ExperimentalTime

/** Imported Sources: stats, integration rows, sync activity log. */
@OptIn(ExperimentalTime::class)
@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) { viewModel.onIntent(SourcesIntent.Initialize) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShellSpacing.lg, vertical = ShellSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
        ) {
            ShellScreenToolbar(
                title = SourcesStringRes.TITLE,
                subtitle = SourcesStringRes.SUBTITLE,
                trailingContent = {
                    ShellPrimaryButton(
                        text = SourcesStringRes.ADD_INTEGRATION,
                        icon = IconPlus,
                        onClick = {},
                        enabled = !state.isBusy,
                    )
                },
            )

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
                        Box(modifier = Modifier.weight(1f)) {
                            card(Modifier.fillMaxWidth())
                        }
                    }
                }
            } else {
                statCards.forEach { card -> card(Modifier.fillMaxWidth()) }
            }

            Text(SourcesStringRes.INTEGRATIONS, style = ShellTheme.typography.sectionTitle, color = colors.textPrimary)
            state.sources.forEach { source ->
                IntegrationRow(
                    source = source,
                    isSyncing = source.id in state.syncingSourceIds,
                    onSync = { viewModel.onIntent(SourcesIntent.Sync(source.id)) },
                    onReconnect = { viewModel.onIntent(SourcesIntent.Reconnect(source.id)) },
                    actionsEnabled = !state.isBusy,
                )
            }
            SyncLogPanel(entries = state.syncLog)
        }

        if (state.isLoading) {
            ShellLoadingOverlay(message = SourcesStringRes.LOADING)
        } else {
            state.loadingMessage?.let { message ->
                ShellLoadingOverlay(message = message)
            }
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(SourcesIntent.DismissError) },
        )
    }
}
