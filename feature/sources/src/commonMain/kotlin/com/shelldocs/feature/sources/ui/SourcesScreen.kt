package com.shelldocs.feature.sources.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.atoms.ShellPrimaryButton
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.molecules.ShellScreenToolbar
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.feature.sources.SourcesStringRes
import com.shelldocs.feature.sources.presentation.SourcesIntent
import com.shelldocs.feature.sources.presentation.SourcesViewModel

/** Connections: real-time status of Ollama, Confluence, Jira, Azure DevOps, Database. */
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
                    ShellGhostButton(
                        text = "Refresh",
                        icon = IconRefresh,
                        onClick = { viewModel.onIntent(SourcesIntent.Refresh) },
                        enabled = !state.isBusy,
                    )
                },
            )

            state.connections.forEach { connection ->
                ConnectionRow(connection = connection)
            }

            ShellPrimaryButton(
                text = SourcesStringRes.CONTACT_SUPPORT,
                onClick = { viewModel.onIntent(SourcesIntent.ContactSupport) },
                enabled = !state.isBusy,
            )
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
