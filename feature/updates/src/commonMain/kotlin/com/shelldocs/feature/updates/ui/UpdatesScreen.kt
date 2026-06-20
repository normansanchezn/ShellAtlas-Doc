package com.shelldocs.feature.updates.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.shelldocs.core.common.testing.DemoTestTags
import com.shelldocs.core.designsystem.atoms.ShellGhostButton
import com.shelldocs.core.designsystem.icons.IconRefresh
import com.shelldocs.core.designsystem.molecules.ShellErrorDialog
import com.shelldocs.core.designsystem.molecules.ShellLoadingOverlay
import com.shelldocs.core.designsystem.theme.ShellTheme
import com.shelldocs.core.designsystem.tokens.ShellSpacing
import com.shelldocs.core.domain.entity.document.MetadataAttribute
import com.shelldocs.feature.updates.presentation.DocumentationHealthTab
import com.shelldocs.feature.updates.presentation.UpdatesIntent
import com.shelldocs.feature.updates.presentation.UpdatesViewModel

/** Documentation Health: risk summary + triage table, with a Metadata Issues section. */
@Composable
fun UpdatesScreen(
    viewModel: UpdatesViewModel,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val colors = ShellTheme.colors
    var editTarget by remember { mutableStateOf<Pair<String, MetadataAttribute>?>(null) }

    LaunchedEffect(viewModel) { viewModel.onIntent(UpdatesIntent.Initialize) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(ShellSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ShellSpacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Documentation Health", style = ShellTheme.typography.pageTitle, color = colors.textPrimary)
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
                    enabled = !state.isScanning && !state.isLoading,
                    modifier = Modifier.testTag(DemoTestTags.UpdatesScan),
                )
            }

            DocumentationHealthTabRow(
                selectedTab = state.selectedTab,
                metadataIssueCount = state.metadataIssuesRequiringAttention,
                onSelect = { tab -> viewModel.onIntent(UpdatesIntent.SelectTab(tab)) },
            )

            when (state.selectedTab) {
                DocumentationHealthTab.HEALTH -> {
                    RiskSummaryRow(state = state, onIntent = viewModel::onIntent, isWide = isWide)
                    UpdatesTable(
                        state = state,
                        isWide = isWide,
                        onSetRisk = { documentId, risk -> viewModel.onIntent(UpdatesIntent.SetManualRisk(documentId, risk)) },
                    )
                }
                DocumentationHealthTab.METADATA_ISSUES -> {
                    MetadataIssuesTable(
                        issues = state.metadataIssues,
                        isAdmin = state.isAdmin,
                        isWide = isWide,
                        onAcceptSuggestion = { documentId, attribute ->
                            viewModel.onIntent(UpdatesIntent.AcceptMetadataSuggestion(documentId, attribute))
                        },
                        onEditMetadata = { documentId, attribute -> editTarget = documentId to attribute },
                    )
                }
            }
        }

        when {
            state.isLoading -> ShellLoadingOverlay(message = "Loading pending updates...")
            state.isScanning -> ShellLoadingOverlay(message = "Scanning documentation health...")
            state.isLoadingMetadataIssues && state.selectedTab == DocumentationHealthTab.METADATA_ISSUES ->
                ShellLoadingOverlay(message = "Classifying documents...")
        }
    }

    state.errorDialog?.let { dialog ->
        ShellErrorDialog(
            state = dialog,
            onDismiss = { viewModel.onIntent(UpdatesIntent.DismissError) },
        )
    }

    editTarget?.let { (documentId, attribute) ->
        EditMetadataDialog(
            documentId = documentId,
            attribute = attribute,
            onDismiss = { editTarget = null },
            onConfirm = { id, attr, value ->
                viewModel.onIntent(UpdatesIntent.AssignMetadata(id, attr, value))
                editTarget = null
            },
        )
    }
}
