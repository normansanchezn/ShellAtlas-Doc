package com.shelldocs.feature.updates.ui

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
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.updates.presentation.UpdatesIntent
import com.shelldocs.feature.updates.presentation.UpdatesViewModel

/** Updates Pending: risk summary cards + maintenance triage table. */
@Composable
fun UpdatesScreen(
    viewModel: UpdatesViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors

    LaunchedEffect(viewModel) { viewModel.onIntent(UpdatesIntent.Initialize) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(ShellSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Updates Pending", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
                Text(
                    text = "${state.updates.size} documents need attention · Maintenance triage",
                    style = ShellTheme.typography.caption,
                    color = colors.textMuted,
                )
            }
            ShellGhostButton(
                text = if (state.isScanning) "Scanning..." else "Scan now",
                icon = IconRefresh,
                onClick = { viewModel.onIntent(UpdatesIntent.ScanNow) },
            )
        }
        RiskSummaryRow(state = state, onIntent = viewModel::onIntent, isWide = isWide)
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                style = ShellTheme.typography.caption,
                color = colors.danger,
            )
        }
        UpdatesTable(state = state, isWide = isWide)
    }
}
